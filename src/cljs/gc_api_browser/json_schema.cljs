(ns gc-api-browser.json-schema
  (:require [clojure.set :refer [project]]
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

(defn- schema->action-node [schema resource action]
  (let [xform (comp (filter #(= (:title %) action))
                    (map set-example)
                    (map #(select-keys % [:method :href :example])))]
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
  (get-in schema [:links 0 :href]))

(defn resource->actions [schema resource]
  (->> (schema->resource-node schema resource)
       :links
       (map :title)
       sort))

(defn schema->resources [schema]
  (->> (vals (:definitions schema))
       (map :title)
       sort))
