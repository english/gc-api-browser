(ns gc-api-browser.store
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [cognitect.transit :as transit]
            [cljs.core.async :as async]
            [gc-api-browser.utils :refer [throttle]]))

(def store-key "app-state")
(def reader (transit/reader :json))
(def writer (transit/writer :json))

(defn write! [k v]
  (.setItem js/localStorage k (transit/write writer v)))

(defn read! [k]
  (when-let [app-state-str (.getItem js/localStorage k)]
    (transit/read reader app-state-str)))

(defn write-throttled! [c]
  (let [throttled (throttle c 300)]
    (go-loop
      []
      (when-some [state (async/<! throttled)]
        (write! store-key state)
        (recur)))))
