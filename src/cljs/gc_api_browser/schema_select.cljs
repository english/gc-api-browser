(ns gc-api-browser.schema-select
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.json :as gjson]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]))

(defn uri->key-path [s]
  (map keyword (rest (.split s "/"))))

(defn schema-val [s v]
  (if (and (map? v)
           (= (keys v) [:$ref]))
    (schema-val s (get-in s (uri->key-path (:$ref v))))
    v))

(defn schema->resource-node [schema resource]
  (->> (:definitions schema)
       vals
       (filter #(= (:title %) resource))
       first))

(defn format-example [example]
  (let [example-body-string (-> (re-find #"(.+) (.+) (.+)\n((.|\n)*)\n\n" example)
                                butlast
                                last)]
    (.stringify js/JSON (.parse js/JSON example-body-string) nil 2)))

(defn schema->action-node [schema resource action]
  (let [action (->> (schema->resource-node schema resource)
                    :links
                    (filter #(= (:title %) action))
                    first)]
    (update-in action [:example] format-example)))

(defn schema->domain [schema]
  (get-in schema [:links 0 :href]))

(defn process-href [href schema]
  (let [[match before pointer after] (re-find #"(.*)\{\((.*)\)\}(.*)" (js/decodeURIComponent href))]
    (if match
      (str before
           (get-in schema (map keyword (-> (.split pointer "/")
                                           rest
                                           vec
                                           (conj :example))))
           after)
      href)))

(defn request-for [schema resource action]
  (let [prefix (schema->domain schema)
        {:keys [method href example]} (schema->action-node schema resource action)]
    {:method method
     :url (str prefix (process-href href schema))
     :body (when (not= method "GET")
             example)}))

(defn resource->actions [schema resource]
  (->> (schema->resource-node schema resource)
       :links
       (map :title)))

(defn schema->resources [schema]
  (->> (vals (:definitions schema))
       (map :title)
       (map name)))

(defn store-schema! [json]
  (.setItem js/localStorage "schema" (.stringify js/JSON json)))

(defn read-as-text [file c]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader) #(put! c (.. % -target -result)))
    (.readAsText reader file)
    c))

(defn set-selected-action! [form schema resource action]
  (om/update! form :selected-action action)
  (om/transact! form (fn [m] (merge m (request-for schema resource action)))))

(defn set-selected-resource! [form schema resource]
  (om/update! form :selected-resource resource)
  (set-selected-action! form schema resource (first (resource->actions schema resource))))

(defn set-schema! [form json]
  (let [schema (js->clj json :keywordize-keys true)]
    (doto form
      (om/update! :schema schema)
      (om/update! :text (:description schema))
      (set-selected-resource! schema (first (schema->resources schema))))))

; (defn handle-schema-input-change [form evt]
;   (let [file (first (array-seq (.. evt -target -files)))]
;     (go (let [text (<! (read-as-text file (chan)))]
;           (.resolveRefs js/JsonRefs (gjson/parse text)
;                         (fn [err json]
;                           (if err
;                             (throw err)
;                             (do
;                               (set-schema! form json)
;                               (store-schema! json)))))))))

(defn handle-schema-input-change [form evt]
  (let [file (first (array-seq (.. evt -target -files)))]
    (go (let [json (gjson/parse (<! (read-as-text file (chan))))]
          (set-schema! form json)
          (store-schema! json)))))

(defn handle-resource-change [form e]
  (set-selected-resource! form (:schema form) (.. e -target -value)))

(defn handle-action-change [{:keys [schema selected-resource] :as form} e]
  (set-selected-action! form schema selected-resource (.. e -target -value)))

(defn schema-file [form]
  (dom/div
    #js {:className "request-form--field request-form--field__schema"}
    (dom/input #js {:type "file"
                    :className "input"
                    :accept "application/json"
                    :onChange (partial handle-schema-input-change form)})))

(defn resource-selection [{:keys [selected-resource selected-action schema] :as form}]
  (dom/div #js {:className "request-form--field request-form--field__resource select-container"}
           (apply dom/select #js {:className "input select-container__select"
                                  :value selected-resource
                                  :onChange (partial handle-resource-change form)}
                  (map #(dom/option #js {:value %} %)
                       (schema->resources schema)))))

(defn action-selection [{:keys [selected-resource selected-action schema] :as form}]
  (dom/div #js {:className "request-form--field request-form--field__action select-container"}
           (apply dom/select #js {:className "input select-container__select"
                                  :value (when selected-action (name selected-action))
                                  :onChange (partial handle-action-change form)}
                  (map #(dom/option #js {:value %} %)
                       (resource->actions schema selected-resource)))))

(defn component [form owner]
  (reify
    om/IWillMount
    (will-mount [_]
      ;; Hack! See https://github.com/omcljs/om/issues/336
      (js/setTimeout
        (fn []
          (when-let [json (.getItem js/localStorage "schema")]
            (set-schema! form (.parse js/JSON json))))))
    om/IRender
    (render [_]
      (dom/div #js {:className "schema-select u-direction-row"}
               (schema-file form)
               (resource-selection form)
               (action-selection form)))))
