(ns gc-api-browser.store
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cognitect.transit :as transit]))

(def store-key "app-state")
(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn write! [object]
  (.setItem js/localStorage store-key (transit/write writer object)))

(defn read! []
  (when-let [app-state-str (.getItem js/localStorage store-key)]
    (transit/read reader app-state-str)))
