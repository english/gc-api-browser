(ns load-test-om.load-test-statistics
  (:require [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]
            [load-test-om.freq :as freq]))

(defn load-test-statistics [data-points]
  (let [response-times (map :response-time data-points)
        freq-map (frequencies response-times)]
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
                                (dom/td nil (Math/round (freq/mean freq-map)))
                                (dom/td nil (freq/quantile freq-map 50 100))
                                (dom/td nil (freq/quantile freq-map 75 100))
                                (dom/td nil (freq/quantile freq-map 95 100)))
                        (dom/tr nil
                                (dom/td nil "Hit Rate")
                                (dom/td nil (str (utils/avg-hit-rate data-points) "/s")))))))
