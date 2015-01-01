(ns load-test-om.hit-table
  (:require [om.dom :as dom :include-macros true]))

(defn hit-table [data-points]
  (let [rows (->> (group-by :status data-points)
                  (map (fn [[status values]]
                         (dom/tr #js {:key status}
                                 (dom/td nil status)
                                 (dom/td nil (count values))))))]
    (apply dom/table nil
               (dom/tr nil
                       (dom/th nil "Status Code")
                       (dom/th nil "Hits"))
               rows)))
