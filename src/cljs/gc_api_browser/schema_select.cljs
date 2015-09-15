(ns gc-api-browser.schema-select
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.json :as gjson]
            [goog.Uri :as uri]
            [cljs.core.async :refer [put! chan <!]]
            [gc-api-browser.utils :refer [log]]
            [gc-api-browser.json-schema :as json-schema]))

(defn get-domain [schema request-cursor]
  (if-let [url-str (:url request-cursor)]
    (let [uri (uri/parse url-str)]
      ;; keep the previously used domain, just remove the path
      (.replace url-str (.getPath uri) ""))
    (json-schema/schema->domain schema)))

(defn request-for [schema resource action request-cursor]
  (let [{:keys [method href example]} (json-schema/schema->action-node schema resource action)]
    {:method method
     :url    (str (get-domain schema request-cursor) (json-schema/process-href href schema))
     :body   (when (not= method "GET") example)}))

(defn read-as-text [file c]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader) (fn [e] (put! c (.. e -target -result))))
    (.readAsText reader file)
    c))

(defn set-selected-action! [app schema resource action]
  (om/update! app :selected-action action)
  (om/transact! app :request (fn [m] (merge m (request-for schema resource action (:request app))))))

(defn set-selected-resource! [app schema resource]
  (om/update! app :selected-resource resource)
  (set-selected-action! app schema resource (first (json-schema/resource->actions schema resource))))

(defn set-schema! [app json]
  (let [schema (js->clj json :keywordize-keys true)]
    (doto app
      (om/update! :schema schema)
      (om/update! :text (:description schema))
      (set-selected-resource! schema (first (json-schema/schema->resources schema))))))

(defn handle-schema-input-change [app evt]
  (let [file (first (array-seq (.. evt -target -files)))]
    (go
     (let [text (<! (read-as-text file (chan)))
           json (gjson/parse text)]
       (set-schema! app json)))))

(defn handle-resource-change [app e]
  (set-selected-resource! app (:schema app) (.. e -target -value)))

(defn handle-action-change [{:keys [schema selected-resource] :as app} e]
  (set-selected-action! app schema selected-resource (.. e -target -value)))

(defn component [cursor _]
  (reify om/IRender
    (render [_]
      (dom/div #js {:className "u-justify-center"}
               (dom/input #js {:type      "file"
                               :className "add-schema"
                               :accept    "application/json"
                               :onChange  (partial handle-schema-input-change cursor)})))))
