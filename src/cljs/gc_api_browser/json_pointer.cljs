(ns gc-api-browser.json-pointer
  (:refer-clojure :exclude [get-in]))

(defn string->keyword [s]
  (if (string? s)
    (keyword s)
    s))

(defn ref-node? [x]
  (and (map? x)
       (= [:$ref] (keys x))))

(defn ref->path [m]
  (-> (:$ref m) (.split "/") rest))

(defn get-in
  "Given a map, this will traverse the map with the given vec of
  keys, but also follow $ref pointers"
  ([m path]
   (get-in m path m))
  ([original-map path current-val]
   (cond
     (ref-node? current-val) (let [new-path (mapv string->keyword
                                                  (concat (ref->path current-val) path))]
                               (get-in original-map new-path))
     path (get-in original-map (next path) (get current-val (first path)))
     :else current-val)))
