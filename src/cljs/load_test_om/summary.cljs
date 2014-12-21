(ns load-test-om.summary
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]))

(defn summary [load-test minimised?]
  (let [response-times (map :response-time (:data-points load-test))
        response-time-avg (/ (apply + response-times) (count response-times))
        hit-rate-avg (utils/avg-hit-rate (:data-points load-test))]
    (dom/div #js {:className "summary"}
             (str "Avg: " (Math/round response-time-avg) "ms, "
                  "Min " (apply min response-times) "ms, "
                  "Max " (apply max response-times) "ms, "
                  "Hit Rate: " hit-rate-avg "/sec"))))
