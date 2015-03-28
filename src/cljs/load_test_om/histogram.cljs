(ns load-test-om.histogram
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljsjs.d3]))

(def chart-width 446)
(def chart-height 150)

(defn x-axis [x-scale]
  (-> (.-svg js/d3)
      .axis
      (.scale x-scale)
      (.orient "bottom")))

(defn get-y-scale [data]
  (-> (.. js/d3 -scale linear)
      (.domain #js [0 (.max js/d3 data #(.-y %))])
      (.range #js [chart-height 0])))

(defn get-x-scale [response-times]
  (-> (.. js/d3 -scale linear)
      (.domain #js [0 (apply max response-times)])
      (.range #js [0 chart-width])))

(defn format-label [x]
  (let [v (.-y x)]
    (when (pos? v) v)))

(defn update-chart [el data-points]
  (let [response-times (map :response-time data-points)
        x-scale (get-x-scale response-times)

        data ((-> (.. js/d3 -layout histogram)
                  (.bins (.ticks x-scale 20)))
              (apply array response-times))

        y-scale (get-y-scale data)

        svg (.select (.select js/d3 el) "svg g")

        bar (-> svg
                (.selectAll ".bar")
                (.data data)
                (.attr "transform" #(str "translate(" (x-scale (.-x %)) "," (y-scale (.-y %)) ")")))]

    ;; new bars
    (doto (-> bar
              (.enter)
              (.append "g")
              (.attr "class" "bar")
              (.attr "transform" #(str "translate(" (x-scale (.-x %)) "," (y-scale (.-y %)) ")")))

      ;; new rects
      (-> (.append "rect")
          (.attr "x" 1)
          (.attr "width" (dec (x-scale (.-dx (aget data 0)))))
          (.attr "height" #(- chart-height (y-scale (.-y %)))))

      ;; new texts
      (-> (.append "text")
          (.attr "dy" "0.75em")
          (.attr "y" 6)
          (.attr "x" (/ (x-scale (.-dx (aget data 0))) 2))
          (.attr "text-anchor" "middle")
          (.text format-label)))

    ;; existing texts
    (-> svg
        (.selectAll ".bar text")
        (.data data)
        (.attr "x" (/ (x-scale (.-dx (aget data 0))) 2))
        (.text format-label)
        (.exit)
        (.remove))

    ;; existing rects
    (-> svg
        (.selectAll "rect")
        (.data data)
        (.attr "width" (dec (x-scale (.-dx (aget data 0)))))
        (.attr "height" #(- chart-height (y-scale (.-y %))))
        (.exit)
        (.remove))

    ;; cleanup old bars
    (-> bar .exit .remove)

    (-> svg
        (.select ".x.axis")
        (.call (x-axis x-scale)))))

(defn create-x-axis [el]
  (-> (.select js/d3 el)
      (.select "svg g")
      (.append "g")
      (.attr "class" "x axis")
      (.attr "transform" (str "translate(0," chart-height ")"))))

(defn create-chart [el data-points]
  (create-x-axis el)
  (update-chart el data-points))

(defn component [load-test owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (create-chart (om/get-node owner) (:data-points load-test)))

    om/IDidUpdate
    (did-update [_ _ _]
      (update-chart (om/get-node owner) (:data-points load-test)))

    om/IRender
    (render [_]
      (let [top 20
            right 10
            bottom 30
            left 30]
        (dom/div #js {:className "chart response-time-histogram"}
                 (dom/svg #js {:width (+ chart-width left right)
                               :height (+ chart-height top bottom)}
                          (dom/g #js {:transform (str "translate(" left "," top ")")})))))))
