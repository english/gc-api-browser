(ns gc-api-browser.schema-select
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.json :as gjson]
            [cljs.core.async :refer [put! chan <!]]))

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

(defn set-selected-action! [request schema resource action]
  (om/update! request :selected-action action)
  (om/transact! request (fn [m] (merge m (request-for schema resource action)))))

(defn set-selected-resource! [request schema resource]
  (om/update! request :selected-resource resource)
  (set-selected-action! request schema resource (first (resource->actions schema resource))))

(defn set-schema! [request json]
  (let [schema (js->clj json :keywordize-keys true)]
    (doto request
      (om/update! :schema schema)
      (om/update! :text (:description schema))
      (set-selected-resource! schema (first (schema->resources schema))))))

(defn handle-schema-input-change [request evt]
  (let [file (first (array-seq (.. evt -target -files)))]
    (go (let [text (<! (read-as-text file (chan)))]
          (.resolveRefs js/JsonRefs (gjson/parse text)
                        (fn [err json]
                          (if err
                            (throw err)
                            (do
                              (set-schema! request json)
                              (store-schema! json)))))))))

(defn handle-resource-change [request e]
  (set-selected-resource! request (:schema request) (.. e -target -value)))

(defn handle-action-change [{:keys [schema selected-resource] :as request} e]
  (set-selected-action! request schema selected-resource (.. e -target -value)))

(defn schema-file [request]
  (dom/div
    #js {:className "u-justify-center"}
    (dom/input #js {:type "file"
                    :className "add-schema"
                    :accept "application/json"
                    :onChange (partial handle-schema-input-change request)})))

(defn resource-selection [{:keys [selected-resource selected-action schema] :as request}]
  (dom/div #js {:className "request-form--field request-form--field__resource select-container flex-item"}
           (apply dom/select #js {:className "input select-container__select"
                                  :value selected-resource
                                  :onChange (partial handle-resource-change request)}
                  (map #(dom/option #js {:value %} %)
                       (schema->resources schema)))))

(defn edit-url [{:keys [url] :as cursor}]
  (dom/div #js {:className "text-mono u-size2of3"}
           (dom/input #js {:className "url-bar input text-mono u-flex-none"
                           :value url
                           :onChange #(om/update! cursor :url (.. % -target -value))})))

(defn edit-method [{:keys [method] :as cursor}]
  (dom/div #js {:className "request-form--field request-form--field__method"}
           (apply dom/select #js {:className "input"
                                  :value method
                                  :onChange #(om/update! cursor :method (.. % -target -value))}
                  (map #(dom/option #js {:value %} %) ["GET" "POST" "PUT"]))))

(defn action-selection [{:keys [selected-resource selected-action schema] :as request}]
  (dom/div #js {:className "request-form--field request-form--field__action select-container"}
           (apply dom/select #js {:className "input select-container__select"
                                  :value (when selected-action (name selected-action))
                                  :onChange (partial handle-action-change request)}
                  (map #(dom/option #js {:value %} %)
                       (resource->actions schema selected-resource)))))

(defn component [request _]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "schema-select u-direction-row"}
               (schema-file request)
               (dom/div #js {:className "u-direction-row"}
                        (edit-url request)
                        (edit-method request))
               (resource-selection request)
               (action-selection request)))))
