(ns gc-api-browser.schema-example
  (:require [clojure.string :as string])
  (:import [goog.format JsonPrettyPrinter]))

(defn prettify [schema-example-str]
  (let [example-json-str (string/join (take-while #(not (.startsWith % "H"))
                                                  (drop-while #(not (.startsWith % "{"))
                                                              (string/split schema-example-str #"\n"))))]
    (try
      (.format (JsonPrettyPrinter.) example-json-str)
      (catch js/Error _
        (js/console.error "Bad example:" schema-example-str)
        example-json-str))))
