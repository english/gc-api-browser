(ns gc-api-browser.store
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cognitect.transit :as transit]))

(def store-key "app-state")
(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn write! [k v]
  (.setItem js/localStorage k (transit/write writer v)))

(defn read! [k]
  (when-let [app-state-str (.getItem js/localStorage k)]
    (transit/read reader app-state-str)))