(ns load-test-om.load-tests
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [load-test-om.summary :as summary]
            [load-test-om.freq :as freq]
            [load-test-om.utils :as utils]
            [load-test-om.hit-table :as hit-table]))

(def firebase-url "https://flickering-heat-5516.firebaseio.com/loadTests")

(defn handle-new-load-test [load-tests snapshot]
  (let [v (.val snapshot)
        load-test {:id (.key snapshot)
                   :resource (.-resource v)
                   :action (.-action v)
                   :data-points []}]
    (om/transact! load-tests :items #(conj % load-test))))

(defn handle-new-data-point [load-test snapshot]
  (let [v (.val snapshot)
        data-point {:id (.key snapshot)
                    :response-time (.-responseTime v)
                    :status (.-status v)
                    :time (.-time v)}]
    (om/transact! load-test :data-points #(conj % data-point))))

(defn minimized-view [{:keys [resource action id] :as load-test} owner]
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
                   (dom/div #js {:className "u-pull-end"}
                            (summary/summary load-test (om/get-state owner :minimised?))))))

(defn start-date [data-points]
  (js/Date. (apply min (map :time data-points))))

(defn run-length [data-points]
  (let [times (map :time data-points)]
    (Math/round (- (apply max times)
                   (apply min times)))))

(defn detailed-summary [load-test]
  (let [response-times (map :response-time (:data-points load-test))
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
                                (dom/td nil (str (utils/avg-hit-rate (:data-points load-test)) "/s")))))))

(defn detail-view [{:keys [resource action id data-points] :as load-test} owner]
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
                             "response-time-chart")
                    (dom/div #js {:className "clearfix"}))

           (dom/hr nil)

           (dom/div #js {:className "third"} (detailed-summary load-test))
           (dom/div #js {:className "third"} (hit-table/hit-table data-points))
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
      (let [fb-ref (js/Firebase. (str firebase-url "/" (:id load-test) "/dataPoints"))]
        (om/set-state! owner :firebase-ref fb-ref)
        (.on fb-ref "child_added" (partial handle-new-data-point load-test))))

    om/IWillUnmount
    (will-unmount [_]
      (.off (om/get-state owner :firebase-ref)))

    om/IRender
    (render [_]
      (dom/li #js {:className (str "well load-test "
                                   (when (om/get-state owner :minimised?) "minimised"))}
              (if (om/get-state owner :minimised?)
                (minimized-view load-test owner)
                (detail-view load-test owner))))))

(defn load-tests [load-tests owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [fb-ref (js/Firebase. firebase-url)]
        (om/set-state! owner :firebase-ref fb-ref)
        (.on fb-ref "child_added" (partial handle-new-load-test load-tests))))

    om/IWillUnmount
    (will-unmount [_]
      (.off (om/get-state owner :firebase-ref))
      (om/update! load-tests :items []))

    om/IRender
    (render [_]
      (apply dom/ul nil (om/build-all load-test (:items load-tests))))))
