(ns load-test-om.summary
  (:require [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]))

(defn summary [{:keys [data-points stats]}]
  (dom/div #js {:className "summary"}
           (str "Avg: " (Math/round (:mean stats)) "ms, "
                "Min " (Math/round (:min stats)) "ms, "
                "Max " (Math/round (:max stats)) "ms, "
                "Hit Rate: " (utils/avg-hit-rate data-points) "/sec")))
