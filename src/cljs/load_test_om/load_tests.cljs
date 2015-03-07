(ns load-test-om.load-tests
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [load-test-om.load-test :as lt-load-test]))

(defn component [load-tests owner]
  (reify
    om/IRender
    (render [_]
      (->> (vals load-tests)
           (sort-by :id)
           reverse
           (map-indexed (fn [i load-test]
                          (om/build lt-load-test/component load-test {:init-state {:minimised? (pos? i)}
                                                                      :key :id})))
           (apply dom/ul nil)))))
