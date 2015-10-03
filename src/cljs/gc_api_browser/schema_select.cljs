(ns gc-api-browser.schema-select
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.set :refer [rename-keys]]
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
  (-> (json-schema/schema->request schema resource action)
      (update :path #(str (get-domain schema request-cursor) %))
      (rename-keys {:path :url})))

(defn set-selected-action! [app schema resource action]
  (om/update! app :selected-action action)
  (om/transact! app :request (fn [m] (merge m (request-for schema resource action (:request app))))))

(defn set-selected-resource! [app schema resource]
  (om/update! app :selected-resource resource)
  (set-selected-action! app schema resource (first (json-schema/resource->actions schema resource))))

(defn set-schema! [app schema]
  (let [sub-schema (select-keys schema [:definitions :links])]
    (doto app
      (om/update! :schema sub-schema)
      (set-selected-resource! schema (first (json-schema/schema->resources schema))))))

(defn- read-as-text [file c]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader) (fn [e] (put! c (.. e -target -result))))
    (.readAsText reader file)
    c))

(defn handle-schema-input-change [app evt]
  (let [file (first (array-seq (.. evt -target -files)))]
    (go
     (let [text (<! (read-as-text file (chan)))
           json (gjson/parse text)]
       (set-schema! app (js->clj json :keywordize-keys true))))))

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
