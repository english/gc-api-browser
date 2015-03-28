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

(defn render-bar [x-scale y-scale bar]
  (dom/g #js {:transform (str "translate("
                              (x-scale (.-x bar)) ","
                              (y-scale (.-y bar)) ")")
              :className "bar"}


         (dom/rect #js {:width (dec (x-scale (.-dx bar)))
                        :height (- chart-height (y-scale (.-y bar)))})

         (dom/text #js {:dy "0.75em"
                        :y 6
                        :x (/ (x-scale (.-dx bar)) 2)
                        :textAnchor "middle"}
                   (when (pos? (.-y bar)) (.-y bar)))))

(defn render-x-axis-path []
  (dom/path #js {:className "domain" :d (str "M0,6V0H" chart-width "V6")}))

(defn render-x-axis-ticks [data x-scale]
  (let [tick-arguments (array 10)
        ticks (if (.-ticks x-scale)
                (.apply (.-ticks x-scale) x-scale tick-arguments)
                (.domain x-scale))]
    (apply dom/g nil (map (fn [tick]
                            (dom/g #js {:className "tick" :transform (str "translate(" (x-scale tick) ",0)")}
                                   (dom/line #js {:x2 0 :y2 6})
                                   (dom/text #js {:dy ".71em" :y 9 :x 0 :style #js {:textAnchor "middle"}} tick)))
                          ticks))))

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
                                 (dom/g #js {:className "x axis" :transform (str "translate(0," chart-height ")")}
                                        (render-x-axis-path)
                                        (render-x-axis-ticks data x-scale))
                                 (map #(render-bar x-scale y-scale %) data))))))))
