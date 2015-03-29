(ns load-test-client.histogram
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljsjs.d3]
            [load-test-client.x-axis :as x-axis]
            [load-test-client.bar :as bar]))

(defn get-y-scale [data height]
  (-> (.. js/d3 -scale linear)
      (.domain #js [0 (.max js/d3 data #(.-y %))])
      (.range #js [height 0])))

(defn get-x-scale [response-times width]
  (-> (.. js/d3 -scale linear)
      (.domain #js [0 (apply max response-times)])
      (.range  #js [0 width])))

(defn component [{:keys [data-points width height]} owner]
  (reify
    om/IRender
    (render [_]
      (let [top 20
            right 10
            bottom 30
            left 30

            response-times (map :response-time data-points)
            x-scale (get-x-scale response-times width)

            data ((-> (.. js/d3 -layout histogram)
                      (.bins (.ticks x-scale 20)))
                  (apply array response-times))

            y-scale (get-y-scale data height)]

        (dom/div #js {:className "chart response-time-histogram"}
                 (dom/svg #js {:width (+ width left right)
                               :height (+ height top bottom)}
                          (apply dom/g #js {:transform (str "translate(" left "," top ")")}
                                 (om/build x-axis/component {:chart-height height :scale x-scale})
                                 (om/build-all #(bar/component % x-scale y-scale height) data))))))))
