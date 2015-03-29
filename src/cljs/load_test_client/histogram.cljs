(ns load-test-client.histogram
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljsjs.d3]
            [load-test-client.x-axis :as x-axis]))

(def chart-width 446)
(def chart-height 150)

(defn get-y-scale [data]
  (-> (.. js/d3 -scale linear)
      (.domain #js [0 (.max js/d3 data #(.-y %))])
      (.range #js [chart-height 0])))

(defn get-x-scale [response-times]
  (-> (.. js/d3 -scale linear)
      (.domain #js [0 (apply max response-times)])
      (.range #js [0 chart-width])))

(defn bar-component [bar x-scale y-scale height]
  (reify
    om/IRender
    (render [_]
      (let [scaled-x (x-scale (.-x bar))
            scaled-y (y-scale (.-y bar))
            scaled-dx (x-scale (.-dx bar))]
        (dom/g #js {:transform (str "translate(" scaled-x "," scaled-y ")")
                    :className "bar"}
               (dom/rect #js {:width (dec scaled-dx)
                              :height (- height scaled-y)})
               (dom/text #js {:dy "0.75em"
                              :y 6
                              :x (/ scaled-dx 2)
                              :textAnchor "middle"}
                         (when (pos? (.-y bar)) (.-y bar))))))))

(defn component [{:keys [data-points]} owner]
  (reify
    om/IRender
    (render [_]
      (let [top 20
            right 10
            bottom 30
            left 30

            response-times (map :response-time data-points)
            x-scale (get-x-scale response-times)

            data ((-> (.. js/d3 -layout histogram)
                      (.bins (.ticks x-scale 20)))
                  (apply array response-times))

            y-scale (get-y-scale data)]

        (dom/div #js {:className "chart response-time-histogram"}
                 (dom/svg #js {:width (+ chart-width left right)
                               :height (+ chart-height top bottom)}
                          (apply dom/g #js {:transform (str "translate(" left "," top ")")}
                                 (om/build x-axis/component {:chart-height chart-height :scale x-scale})
                                 (om/build-all #(bar-component % x-scale y-scale chart-height) data))))))))
