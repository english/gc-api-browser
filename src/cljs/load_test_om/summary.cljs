(ns load-test-om.summary
  (:require [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]))

(defn mean [xs]
  (Math/round (/ (apply + xs) (count xs))))

(defn summary [data-points]
  (let [response-times (map :response-time data-points)
        hit-rate-avg (utils/avg-hit-rate data-points)]
    (dom/div #js {:className "summary"}
             (str "Avg: " (mean response-times) "ms, "
                  "Min " (apply min response-times) "ms, "
                  "Max " (apply max response-times) "ms, "
                  "Hit Rate: " hit-rate-avg "/sec"))))
