(ns gc-api-browser.schema-example
  (:require [clojure.string :as string])
  (:import [goog.format JsonPrettyPrinter]))

(defn- starts-with? [s prefix]
  (= 0 (.indexOf s prefix)))

(defn- extract-first-json-part [example-str]
  (string/join (->> (string/split example-str #"\n")
                    reverse
                    (drop-while #(not (starts-with? % "H")))
                    rest
                    (take-while #(not (or (starts-with? % "POST")
                                          (starts-with? % "PUT"))))
                    reverse)))

(defn prettify [schema-example-str]
  (let [request-json-str (extract-first-json-part schema-example-str)]
    (try
      (.format (JsonPrettyPrinter.) request-json-str)
      (catch js/Error _
        ""))))
