(ns load-test-client.histogram
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljsjs.d3]))

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

(defn bar-component [bar x-scale y-scale chart-height]
  (reify
    om/IRender
    (render [_]
      (let [scaled-x (x-scale (.-x bar))
            scaled-y (y-scale (.-y bar))
            scaled-dx (x-scale (.-dx bar))]
        (dom/g #js {:transform (str "translate(" scaled-x "," scaled-y ")")
                    :className "bar"}
               (dom/rect #js {:width (dec scaled-dx)
                              :height (- chart-height scaled-y)})
               (dom/text #js {:dy "0.75em"
                              :y 6
                              :x (/ scaled-dx 2)
                              :textAnchor "middle"}
                         (when (pos? (.-y bar)) (.-y bar))))))))

(defn x-axis-path-component [scale]
  (reify
    om/IRender
    (render [_]
      (let [rng (.range scale)]
        (dom/path #js {:className "domain"
                       :d (str "M0" (aget rng 0) ",6V0H" (aget rng 1) "V6")})))))

(defn x-axis-ticks-component [scale]
  (reify
    om/IRender
    (render [_]
      (let [ticks (.apply (.-ticks scale) scale)]
        (apply dom/g nil (map (fn [tick]
                                (dom/g #js {:className "tick" :transform (str "translate(" (scale tick) ",0)")}
                                       (dom/line #js {:x2 0 :y2 6})
                                       (dom/text #js {:dy ".71em" :y 9 :x 0 :style #js {:textAnchor "middle"}} tick)))
                              ticks))))))

(defn x-axis-component [{:keys [chart-height scale]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/g #js {:className "x axis" :transform (str "translate(0," chart-height ")")}
             (om/build x-axis-path-component scale)
             (om/build x-axis-ticks-component scale)))))

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
                                 (om/build x-axis-component {:chart-height chart-height :scale x-scale})
                                 (om/build-all #(bar-component % x-scale y-scale chart-height) data))))))))
