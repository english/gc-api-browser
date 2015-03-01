(ns load-test-om.load-tests
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [goog.json :as gjson]
            [clojure.set :refer [rename]]
            [load-test-om.load-test :as load-test])
  (:import (goog.net WebSocket EventType)))

(defn load-tests [load-tests owner]
  (reify
    om/IRender
    (render [_]
      (->> (vals load-tests)
           (sort-by :id)
           reverse
           (om/build-all load-test/load-test)
           (apply dom/ul nil)))))
