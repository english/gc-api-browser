(ns load-test-om.load-tests
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [goog.json :as gjson]
            [clojure.set :refer [rename]]
            [load-test-om.load-test :as load-test])
  (:import (goog.net WebSocket EventType)))

(defn handle-new-load-test [load-tests data]
  (let [load-test (-> (gjson/parse data)
                      (js->clj :keywordize-keys true)
                      (assoc :data-points []))]
    (.log js/console "handled new load test " (clj->js load-test))
    (om/transact! load-tests :items #(conj % load-test))))

(defn load-tests [load-tests owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [ws (WebSocket.)]
        (events/listen ws WebSocket.EventType.MESSAGE #(handle-new-load-test load-tests (.-message %)))
        (.open ws "ws://localhost:3000/load-tests")
        (om/set-state! owner :ws ws)))

    om/IWillUnmount
    (will-unmount [_]
      (.close (om/get-state owner :ws))
      (om/update! load-tests :items []))

    om/IRender
    (render [_]
      (->> (:items load-tests)
           (sort-by :id)
           reverse
           (map #(om/build load-test/load-test % {:key (:id %)}))
           (apply dom/ul nil)))))
