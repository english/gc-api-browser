(ns load-test-om.statistics-table
  (:require [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]))

(defn statistics-table [{:keys [data-points stats]}]
  (let [[_ median seventy-fifth _ ninety-fifth] (vals (:percentiles stats))]
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
                                (dom/td nil (Math/round (:mean stats)))
                                (dom/td nil median)
                                (dom/td nil seventy-fifth)
                                (dom/td nil ninety-fifth))
                        (dom/tr nil
                                (dom/td nil "Hit Rate")
                                (dom/td nil (str (utils/avg-hit-rate data-points) "/s")))))))
