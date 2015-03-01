(ns load-test-om.load-test
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [goog.json :as gjson]
            [load-test-om.summary :refer [summary]]
            [load-test-om.hit-table :refer [hit-table]]
            [load-test-om.statistics-table :refer [statistics-table]]
            [load-test-om.hit-rate-chart :refer [hit-rate-chart]]
            [load-test-om.histogram :refer [histogram]])
  (:import (goog.net WebSocket EventType)))

(defn start-date [data-points]
  (js/Date. (apply min (map :time data-points))))

(defn run-length [data-points]
  (let [times (map :time data-points)]
    (Math/round (- (apply max times)
                   (apply min times)))))

(defn handle-delete [id]
  #_(when (.confirm js/window "Are you sure?")
    (.remove (js/Firebase. (str firebase-url "/" id)))))

(defn minimized-view [{:keys [resource action id data-points] :as load-test} owner]
  (dom/div #js {:className "minimised-view"}
           (dom/h2 nil
                   (dom/div #js {:className "delete-btn"
                                 :onClick (partial handle-delete id)} "x")
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
                                 :onClick (partial handle-delete id)}, "x")
                   (dom/span #js {:className "capitalize"} resource)
                   "/"
                   (dom/span #js {:className "capitalize"} action)
                   (dom/small #js {:className "capitalize"} id),
                   (dom/div #js {:className "size-toggle-btn u-pull-end"
                                 :onClick #(om/set-state! owner :minimised? true)} "-"))

           (dom/div #js {:className "charts"}
                    (dom/div #js {:className "live-chart--container half"}
                             (dom/h2 nil "Response times")
                             (om/build histogram load-test))
                    (dom/div #js {:className "live-chart--container half"}
                             (dom/h2 nil "Hit rate")
                             (om/build hit-rate-chart load-test))
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

(defn handle-new-data-point [load-test data]
  (let [data-point (-> (gjson/parse data) (js->clj :keywordize-keys true))]
    (om/transact! load-test :data-points #(conj % data-point))))

(defn load-test [load-test owner]
  (reify
    om/IRender
    (render [_]
      (when (pos? (count (:data-points load-test)))
        (dom/li #js {:className (str "well load-test "
                                     (when (om/get-state owner :minimised?) "minimised"))}
                (if (om/get-state owner :minimised?)
                  (minimized-view load-test owner)
                  (load-test-detailed load-test owner)))))))
