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

(defn counted-by-status [data-points]
  (map (fn [[status values]]
         [(str status "s") (count values)])
       (group-by :status data-points)))

(defn start-date [data-points]
  (js/Date. (apply min (map :time data-points))))

(defn component [{:keys [data-points id duration rate] :as load-test}]
  (let [response-times (map :response-time data-points)
        data (concat [["ID" id]
                      ["Date" (.toLocaleString (start-date data-points))]
                      ["Duration" (str duration " seconds")]
                      ["Rate" (str rate "/second")]
                      ["Mean" (Math/round (utils/mean response-times))]
                      ["Median" (percentile response-times 0.5)]
                      ["75th" (percentile response-times 0.75)]
                      ["95th" (percentile response-times 0.95)]
                      ["Hit Rate" (str (utils/avg-hit-rate data-points) "/s")]]
                     (counted-by-status data-points))]
    (dom/div #js {:className "summary"}
             (dom/table nil
                        (apply dom/tr nil
                               (map #(dom/th nil (if (keyword? %)
                                                   (name %)
                                                   %))
                                    (map first data)))
                        (apply dom/tr nil
                               (map #(dom/td nil %) (map second data)))))))
