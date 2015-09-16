(ns gc-api-browser.json-schema
  (:require [gc-api-browser.json-pointer :as json-pointer]
            [gc-api-browser.schema-example :as schema-example]))

(defn- schema->resource-node [schema resource]
  (->> (:definitions schema)
       vals
       (filter #(= (:title %) resource))
       first))

(defn- format-example [example]
  (when (string? example)
    (schema-example/prettify example)))

(defn schema->action-node [schema resource action]
  (let [action (->> (schema->resource-node schema resource)
                    :links
                    (filter #(= (:title %) action))
                    first)
        node  (if (#{"POST" "PUT"} (:method action))
                 (update-in action [:example] format-example)
                 action)]
    (select-keys node [:method :href :example])))

(defn schema->domain [schema]
  (get-in schema [:links 0 :href]))

(defn process-href [href schema]
  (let [[match before pointer after] (re-find #"(.*)\{\((.*)\)\}(.*)" (js/decodeURIComponent href))]
    (if match
      (str before
           (json-pointer/get-in schema (map keyword (-> (.split pointer "/")
                                                        rest
                                                        vec
                                                        (conj :example))))
           after)
      href)))

(defn resource->actions [schema resource]
  (->> (schema->resource-node schema resource)
       :links
       (map :title)
       sort))

(defn schema->resources [schema]
  (->> (vals (:definitions schema))
       (map :title)
       sort))