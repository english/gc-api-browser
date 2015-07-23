(ns gc-api-browser.schema-select
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.json :as gjson]
            [goog.Uri :as uri]
            [cljs.core.async :refer [put! chan <!]]))

(defn string->keyword [s]
  (if (string? s)
    (keyword s)
    s))

(defn ref-node? [x]
  (and (map? x)
       (= [:$ref] (keys x))))

(defn ref->path [m]
  (-> (:$ref m) (.split "/") rest))

(defn schema->value
  "Given a json-schema map, this will traverse the map with the given vec of
  keys, but also follow $ref pointers"
  [schema path]
  (loop [current-val schema
         ks path]
    (cond
      (ref-node? current-val) (recur schema (mapv string->keyword (concat (ref->path current-val) ks)))
      ks (recur (get current-val (first ks)) (next ks))
      :else current-val)))

(defn schema->resource-node [schema resource]
  (->> (:definitions schema)
       vals
       (filter #(= (:title %) resource))
       first))

(defn format-example [example]
  (when (string? example)
    (let [example-body-string (-> (re-find #"(.+) (.+) (.+)\n((.|\n)*)\n\n" example)
                                  butlast
                                  last)]
      (.stringify js/JSON (.parse js/JSON example-body-string) nil 2))))

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
           (schema->value schema (map keyword (-> (.split pointer "/")
                                                  rest
                                                  vec
                                                  (conj :example))))
           after)
      href)))

(defn get-domain [schema request-cursor]
  (if-let [url-str (:url request-cursor)]
    (let [uri (uri/parse url-str)]
      ;; keep the previously used domain, just remove the path
      (.replace url-str (.getPath uri) ""))
    (schema->domain schema)))

(defn request-for [schema resource action request-cursor]
  (let [{:keys [method href example]} (schema->action-node schema resource action)]
    {:method method
     :url    (str (get-domain schema request-cursor) (process-href href schema))
     :body   (when (not= method "GET")
               example)}))

(defn resource->actions [schema resource]
  (->> (schema->resource-node schema resource)
       :links
       (map :title)
       sort))

(defn schema->resources [schema]
  (->> (vals (:definitions schema))
       (map :title)
       (map name)
       sort))

(defn read-as-text [file c]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader) (fn [e]
                              (put! c (.. e -target -result))))
    (.readAsText reader file)
    c))

(defn set-selected-action! [request schema resource action]
  (om/update! request :selected-action action)
  (om/transact! request (fn [m] (merge m (request-for schema resource action request)))))

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
    (go
      (let [text (<! (read-as-text file (chan)))
            json (gjson/parse text)]
        (set-schema! request json)))))

(defn handle-resource-change [request e]
  (set-selected-resource! request (:schema request) (.. e -target -value)))

(defn handle-action-change [{:keys [schema selected-resource] :as request} e]
  (set-selected-action! request schema selected-resource (.. e -target -value)))

(defn schema-file [request]
  (dom/div #js {:className "u-justify-center"}
           (dom/input #js {:type      "file"
                           :className "add-schema"
                           :accept    "application/json"
                           :onChange  (partial handle-schema-input-change request)})))
