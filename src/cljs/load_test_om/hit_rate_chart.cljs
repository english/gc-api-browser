(ns load-test-om.hit-rate-chart
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [load-test-om.utils :as utils]
            [cljsjs.d3]))

(def width 446)
(def height 150)
(def top 20)
(def right 10)
(def bottom 30)
(def left 30)

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

(defn get-scales [domain]
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

(defn update-chart [el data]
  (let [scales (get-scales (domain data))
        axes   (get-axes scales)]
    (draw-points el scales axes data)))

(defn load-test->data [load-test]
  (->> (:data-points load-test)
       (sort-by :time)
       utils/bucket-into-seconds
       (map utils/hit-rate)))

(defn component [load-test owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (update-chart (om/get-node owner) (load-test->data load-test)))

    om/IDidUpdate
    (did-update [_ _ _]
      (update-chart (om/get-node owner) (load-test->data load-test)))

    om/IRender
    (render [_]
      (dom/div #js {:className "chart hit-rate-chart"}
               (dom/svg #js {:className "d3"
                             :width (+ width left right)
                             :height (+ height top bottom)}
                        (dom/g #js {:transform (str "translate(" left "," top ")")}
                               (dom/g #js {:className "x axis"
                                           :transform (str "translate(0, " height ")")})
                               (dom/g #js {:className "y axis"}
                                      (dom/text #js {:transform "rotate(-90)"}))
                               (dom/path #js {:className "line"})))))))
