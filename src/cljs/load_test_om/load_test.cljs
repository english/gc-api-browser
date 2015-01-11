(ns load-test-om.load-test
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [load-test-om.summary :refer [summary]]
            [load-test-om.hit-table :refer [hit-table]]
            [load-test-om.statistics-table :refer [statistics-table]]
            [load-test-om.response-time-chart :refer [response-time-chart]]))

(defn start-date [data-points]
  (js/Date. (apply min (map :time data-points))))

(defn run-length [data-points]
  (let [times (map :time data-points)]
    (Math/round (- (apply max times)
                   (apply min times)))))

(defn minimized-view [{:keys [resource action id data-points] :as load-test} owner]
  (dom/div #js {:className "minimised-view"}
           (dom/h2 nil
                   (dom/div #js {:className "delete-btn"
                                 :onClick (partial println "handle-delete")} "x")
                   (dom/span #js {:className "capitalize"} resource)
                   "/"
                   (dom/span #js {:className "capitalize"} action)
                   (dom/small #js {:className "capitalize"} id)
                   (dom/div #js {:className "size-toggle-btn u-pull-end"
                                 :onClick #(om/set-state! owner :minimised? false)} "+")
                   (dom/div #js {:className "u-pull-end"} (summary load-test)))))

(defn load-test-detailed [{:keys [resource action id data-points] :as load-test} owner]
  (dom/div #js {:className "detail-view"}
           (dom/h2 nil
                   (dom/div #js {:className "delete-btn"
                                 :onClick (partial println "handle-delete")}, "x")
                   (dom/span #js {:className "capitalize"} resource)
                   "/"
                   (dom/span #js {:className "capitalize"} action)
                   (dom/small #js {:className "capitalize"} id),
                   (dom/div #js {:className "size-toggle-btn u-pull-end"
                                 :onClick #(om/set-state! owner :minimised? true)} "-"))

           (dom/div #js {:className "charts"}
                    (dom/div #js {:className "live-chart--container half"}
                             (dom/h2 nil "Hit Rate")
                             "hit-rate-chart")
                    (dom/div #js {:className "live-chart--container half"}
                             (dom/h2 nil "Response Time")
                             (om/build response-time-chart load-test))
                    (dom/div #js {:className "clearfix"}))

           (dom/hr nil)

           (dom/div #js {:className "third"} (statistics-table load-test))
           (dom/div #js {:className "third"} (hit-table data-points))
           (dom/div #js {:className "third"}
                    (dom/table #js {:className "extra-details"}
                               (dom/tr nil
                                       (dom/th nil "ID")
                                       (dom/td nil id))
                               (dom/tr nil
                                       (dom/th nil "Date")
                                       (dom/td nil (.toDateString (start-date data-points))))
                               (dom/tr nil
                                       (dom/th nil "Run length")
                                       (dom/td nil (str (/ (run-length data-points) 1000) "seconds")))))

           (dom/div #js {:className "clearfix"})))

(defn load-test [load-test owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (om/set-state! owner :minimised? true))

    om/IRender
    (render [_]
      (dom/li #js {:className (str "well load-test "
                                   (when (om/get-state owner :minimised?) "minimised"))}
              (if (om/get-state owner :minimised?)
                (minimized-view load-test owner)
                (load-test-detailed load-test owner))))))
