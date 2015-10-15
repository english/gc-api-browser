(ns gc-api-browser.json-schema
  (:require [clojure.set :refer [project]]
            [goog.json :as gjson]
            [goog.object :as gobject]
            [gc-api-browser.utils :refer [log]]
            [gc-api-browser.json-pointer :as json-pointer]
            [gc-api-browser.schema-example :as schema-example]))

(defn- expand-href* [schema [_ before pointer after]]
  (let [path-with-example (-> (.split pointer "/") rest vec (conj :example))]
    (str before
         (json-pointer/get-in schema (map keyword path-with-example))
         after)))

(defn expand-href [href schema]
  (if-let [matches (re-find #"(.*)\{\((.*)\)\}(.*)" (js/decodeURIComponent href))]
    (expand-href* schema matches)
    href))

(defn- schema->resource-node [schema resource]
  (->> (:definitions schema)
       vals
       (filter #(= (:title %) resource))
       first))

(defn set-example [action-node]
  (if (and (#{"POST" "PUT"} (:method action-node))
           (string? (:example action-node)))
    (update action-node :example schema-example/prettify)
    action-node))

(defn schema->action-node [schema resource action]
  (let [xform (comp (filter #(= (:title %) action))
                    (map set-example))]
    (->> (schema->resource-node schema resource)
         :links
         (sequence xform)
         first)))

(defn schema->request [schema resource action]
  (let [{:keys [method href example]} (schema->action-node schema resource action)]
    {:method method
     :path   (expand-href href schema)
     :body   (when (not= method "GET") example)}))

(defn schema->domain [schema]
  ;; use sandbox url
  (get-in schema [:links 1 :href]))

(defn resource->actions [schema resource]
  (->> (schema->resource-node schema resource)
       :links
       (map :title)
       sort))

(defn schema->resources [schema]
  (->> (vals (:definitions schema))
       (map :title)
       sort))

(defn- valid-json? [string]
  (try
    (gjson/parse string)
    true
    (catch js/Object e
      false)))

(defn error->map [error]
  {:message (.-message error)
   :schema-path (.-schemaPath error)
   :data-path (.-dataPath error)})

(defn json-error [string]
  (try
    (js/JSON.parse string)
    (catch js/Object e
      {:valid false
       :errors [{:message (str (.-name e) ": " (.-message e))
                 :schema-path ""
                 :data-path ""}]})))

(defn validate-against-schema [schema resource action request-string]
  (let [action-schema (:schema (schema->action-node schema resource action))
        combined-schema (merge action-schema schema)
        body-object (-> request-string gjson/parse gobject/getValues first)
        result (.validateMultiple js/tv4 body-object (clj->js combined-schema) false true)]
    (update (js->clj result :keywordize-keys true) :errors #(map error->map %))))

(defn validate-request [schema resource action request-string]
  (if (valid-json? request-string)
    (validate-against-schema schema resource action request-string)
    (json-error request-string)))
