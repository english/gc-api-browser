(ns load-test-client.x-axis
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn path [scale]
  (reify
    om/IRender
    (render [_]
      (let [[start end] (.range scale)]
        (dom/path #js {:className "domain" :d (str "M0" start ",6V0H" end "V6")})))))

(defn ticks [scale]
  (reify
    om/IRender
    (render [_]
      (let [ticks (.apply (.-ticks scale) scale)]
        (apply dom/g nil
               (map (fn [tick]
                      (dom/g #js {:className "tick" :transform (str "translate(" (scale tick) ",0)")}
                             (dom/line #js {:x2 0 :y2 6})
                             (dom/text #js {:dy ".71em" :y 9 :x 0 :style #js {:textAnchor "middle"}} tick)))
                    ticks))))))

(defn component [{:keys [chart-height scale]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/g #js {:className "x axis" :transform (str "translate(0," chart-height ")")}
             (om/build path scale)
             (om/build ticks scale)))))
