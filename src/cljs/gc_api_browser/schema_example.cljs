(ns gc-api-browser.schema-example
  (:require [clojure.string :as string])
  (:import [goog.format JsonPrettyPrinter]))

(defn- starts-with? [s prefix]
  (= 0 (.indexOf s prefix)))

(defn- extract-first-json-part [example-str]
  (string/join (->> (string/split example-str #"\n")
                    (drop-while #(not (starts-with? % "{")))
                    (take-while #(not (starts-with? % "H"))))))

(defn prettify [schema-example-str]
  (let [example-json-str (extract-first-json-part schema-example-str)]
    (try
      (.format (JsonPrettyPrinter.) example-json-str)
      (catch js/Error _
        example-json-str))))
