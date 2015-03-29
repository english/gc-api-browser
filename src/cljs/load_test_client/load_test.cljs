(ns load-test-client.load-test
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [goog.json :as gjson]
            [load-test-client.summary :as summary]
            [load-test-client.statistics-table :as statistics-table]
            [load-test-client.hit-rate-chart :as hit-rate-chart]
            [load-test-client.histogram :as histogram])
  (:import (goog.net WebSocket EventType)))

(defn handle-delete [id]
  (.log js/console "handle-delete" id))

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
                   (dom/div #js {:className "u-pull-end"} (summary/component load-test)))))

(defn load-test-detailed [{:keys [resource action id duration rate data-points] :as load-test} owner]
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
                             (om/build histogram/component {:data-points data-points
                                                            :width 446
                                                            :height 150}))
                    (dom/div #js {:className "live-chart--container half"}
                             (dom/h2 nil "Hit rate")
                             (om/build hit-rate-chart/component load-test))
                    (dom/div #js {:className "clearfix"}))

           (dom/hr nil)

           (dom/div nil (statistics-table/component load-test))
           (dom/div #js {:className "clearfix"})))

(defn handle-new-data-point [load-test data]
  (let [data-point (-> (gjson/parse data) (js->clj :keywordize-keys true))]
    (om/transact! load-test :data-points #(conj % data-point))))

(defn component [load-test owner]
  (reify
    om/IRender
    (render [_]
      (when (pos? (count (:data-points load-test)))
        (dom/li #js {:className (str "well load-test "
                                     (when (om/get-state owner :minimised?) "minimised"))}
                (if (om/get-state owner :minimised?)
                  (minimized-view load-test owner)
                  (load-test-detailed load-test owner)))))))
