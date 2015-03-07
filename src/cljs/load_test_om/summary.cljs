(ns load-test-om.summary
  (:require [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]))

(defn component [{:keys [data-points]}]
  (let [response-times (map :response-time data-points)]
    (dom/div #js {:className "summary"}
             (str "Avg: " (Math/round (utils/mean response-times)) "ms, "
                  "Min " (Math/round (apply min response-times)) "ms, "
                  "Max " (Math/round (apply max response-times)) "ms, "
                  "Hit Rate: " (utils/avg-hit-rate data-points) "/sec"))))
