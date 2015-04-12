(ns load-test-client.schema-select
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.json :as gjson]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]]))

(defn schema->resource-node [schema resource]
  (->> (:definitions schema)
       vals
       (filter #(= (:title %) resource))
       first))

(defn schema->action-node [schema resource action]
  (->> (schema->resource-node schema resource)
       :links
       (filter #(= (:title %) action))
       (first)))

(defn schema->domain [schema]
  (get-in schema [:links 0 :href]))

(defn request-for [schema resource action]
  (let [prefix (schema->domain schema)
        action-node (schema->action-node schema resource action)]
    {:method (:method action-node)
     :url (str prefix (:href action-node))}))

(defn resource->actions [schema resource]
  (->> (schema->resource-node schema resource)
       :links
       (map :title)))

(defn read-as-text [file c]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader) #(put! c (.. % -target -result)))
    (.readAsText reader file)
    c))

(defn schema->resources [schema]
  (->> (vals (:definitions schema))
       (map :title)
       (map name)))

(defn set-selected-action! [form schema resource action]
  (om/update! form :selected-action action)
  (om/transact! form (fn [m] (merge m (request-for schema resource action)))))

(defn set-selected-resource! [form schema resource]
  (om/update! form :selected-resource resource)
   (set-selected-action! form schema resource (first (resource->actions schema resource))))

(defn set-schema! [form schema]
  (om/update! form :schema schema)
  (set-selected-resource! form schema (first (schema->resources schema))))

(defn handle-schema-input-change [form evt]
  (let [file (first (array-seq (.. evt -target -files)))]
    (go (let [text (<! (read-as-text file (chan)))]
          (set-schema! form (-> text gjson/parse (js->clj :keywordize-keys true)))))))

(defn handle-resource-change [form e]
  (set-selected-resource! form (:schema form) (.. e -target -value)))

(defn handle-action-change [{:keys [schema selected-resource] :as form} e]
  (set-selected-action! form schema selected-resource (.. e -target -value)))

(defn schema-file [form]
  (dom/div
    #js {:className "load-test-form--field load-test-form--field__schema"}
    (dom/div #js {:className "label"} "Schema:")
    (dom/input #js {:type "file"
                    :className "input"
                    :accept "application/json"
                    :onChange (partial handle-schema-input-change form)})))

(defn resource-selection [{:keys [selected-resource selected-action schema] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__resource"}
           (dom/div #js {:className "label"} "Resource:")
           (apply dom/select #js {:className "input"
                                  :value selected-resource
                                  :onChange (partial handle-resource-change form)}
                  (map #(dom/option #js {:value %} %)
                       (schema->resources schema)))))

(defn action-selection [{:keys [selected-resource selected-action schema] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__action"}
           (dom/div #js {:className "label"} "Action:")
           (apply dom/select #js {:className "input"
                                  :value (when selected-action (name selected-action))
                                  :onChange (partial handle-action-change form)}
                  (map #(dom/option #js {:value %} %)
                       (resource->actions schema selected-resource)))))

(defn component [form owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "schema-select"}
               (schema-file form)
               (resource-selection form)
               (action-selection form)))))
