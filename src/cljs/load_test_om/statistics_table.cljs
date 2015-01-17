(ns load-test-om.statistics-table
  (:require [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]))

(defn percentile [xs ptile]
  {:pre [(not (empty? xs))
         (>= ptile 0)
         (<= ptile 1)]}
  (let [index (* (count xs) ptile)
        values (sort xs)
        rounded-index (Math/round index)]
    (if (= rounded-index index)
      (/ (apply + (subvec values rounded-index (inc rounded-index)))
         2)
      (nth values (dec rounded-index)))))

(defn statistics-table [{:keys [data-points stats]}]
  (let [response-times (map :response-time data-points)]
    (dom/div #js {:className "summary"}
             (dom/table nil
                        (dom/tr nil
                                (dom/th nil "Statistic")
                                (dom/th nil "Mean")
                                (dom/th nil "Median")
                                (dom/th nil "75th")
                                (dom/th nil "95th"))
                        (dom/tr nil
                                (dom/td nil "Response Time")
                                (dom/td nil (Math/round (utils/mean response-times)))
                                (dom/td nil (percentile response-times 0.5))
                                (dom/td nil (percentile response-times 0.75))
                                (dom/td nil (percentile response-times 0.95)))
                        (dom/tr nil
                                (dom/td nil "Hit Rate")
                                (dom/td nil (str (utils/avg-hit-rate data-points) "/s")))))))
