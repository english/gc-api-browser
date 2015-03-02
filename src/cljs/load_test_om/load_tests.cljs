(ns load-test-om.load-tests
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [load-test-om.load-test :as load-test]))

(defn load-tests [load-tests owner]
  (reify
    om/IRender
    (render [_]
      (let [ordered (->> (vals load-tests) (sort-by :id) reverse)]
        (apply dom/ul nil
               (map #(om/build load-test/load-test % {:init-state {:minimised? (not= % (first ordered))}
                                                      :key :id})
                    ordered))))))
