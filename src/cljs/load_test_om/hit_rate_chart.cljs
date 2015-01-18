(ns load-test-om.hit-rate-chart
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]))

(defn draw-points [el scales axes data]
  (let [svg (.select (.select js/d3 el) "svg")
        line (-> (.. js/d3 -svg line)
                 (.x #((:x scales) (.-x %)))
                 (.y #((:y scales) (.-y %))))
        datum (->> data
                   (sort-by :x)
                   (map (fn [m]
                          #js {:x (js/Date. (:x m))
                               :y (:y m)})))]
    (-> svg
        (.select ".x.axis")
        (.call (:x axes)))
    (-> svg
        (.select ".y.axis")
        (.call (:y axes)))
    (-> svg
        (.select "path.line")
        (.datum (apply array datum))
        (.attr "d" line))))

(defn get-scales [domain width height]
  {:x (-> (.. js/d3 -time scale)
          (.domain (apply array (:x domain)))
          (.range #js [0 width]))
   :y (-> (.. js/d3 -scale linear)
          (.domain (apply array (:y domain)))
          (.range #js [height 0]))})

(defn get-axes [scales]
  {:x (-> (.. js/d3 -svg axis)
          (.scale (:x scales))
          (.orient "bottom"))
   :y (-> (.. js/d3 -svg axis)
          (.scale (:y scales))
          (.orient "left"))})

(def min-max (juxt (partial apply min) (partial apply max)))

(defn domain [data]
  {:x (min-max (map :x data))
   :y (min-max (map :y data))})

(defn update-chart [el width height data]
  (let [scales (get-scales (domain data) width height)
        axes   (get-axes scales)]
    (draw-points el scales axes data)))

(defn create-chart [el data]
  (let [width 446
        height 150
        top 20
        right 10
        bottom 30
        left 30
        svg (-> js/d3
                (.select el)
                (.append "svg")
                (.attr "class" "d3")
                (.attr "width" (+ width left right))
                (.attr "height" (+ height top bottom))
                (.append "g")
                (.attr "transform" (str "translate(" left "," top ")")))]
    (-> svg
        (.append "g")
        (.attr "class" "x axis")
        (.attr "transform" (str "translate(0," height ")")))
    (-> svg
        (.append "g")
        (.attr "class" "y axis")
        (.append "text")
        (.attr "transform" "rotate(-90)"))
    (-> svg
        (.append "path")
        (.attr "class" "line"))
    (update-chart el width height data)))

(defn load-test->data [load-test]
  (->> (:data-points load-test)
       (sort-by :time)
       utils/bucket-into-seconds
       (map utils/hit-rate)))

(defn hit-rate-chart [load-test owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (create-chart (om/get-node owner) (load-test->data load-test)))

    om/IRender
    (render [_]
      (dom/div #js {:className "chart hit-rate-chart"}))))
