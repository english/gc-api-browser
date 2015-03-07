(ns load-test-om.histogram
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn x-axis [x-scale]
  (-> (.-svg js/d3)
      .axis
      (.scale x-scale)
      (.orient "bottom")))

(defn get-y-scale [height data]
  (-> (.. js/d3 -scale linear)
      (.domain #js [0 (.max js/d3 data #(.-y %))])
      (.range #js [height 0])))

(defn get-x-scale [width values]
  (-> (.. js/d3 -scale linear)
      (.domain #js [0 (apply max values)])
      (.range #js [0 width])))

(defn create-chart [el load-test]
  (let [height 150]

    ;; axis
    (-> (.select (.select js/d3 el) "svg g")
        (.append "g")
        (.attr "class" "x axis")
        (.attr "transform" (str "translate(0," height ")")))))

(def label-formatter (.format js/d3 ",.0f"))

(defn format-label [x]
  (label-formatter (.-y x)))

(defn update-chart [el load-test]
  (let [width 446
        height 150
        values (apply array (map :response-time (:data-points load-test)))
        x-scale (get-x-scale width values)

        data ((-> (.. js/d3 -layout histogram)
                  (.bins (.ticks x-scale 20)))
              values)

        y-scale (get-y-scale height data)

        svg (.select (.select js/d3 el) "svg g")

        bar (-> svg
                (.selectAll ".bar")
                (.data data)
                (.attr "transform" #(str "translate(" (x-scale (.-x %)) "," (y-scale (.-y %)) ")")))]

    (doto (-> bar
              (.enter)
              (.append "g")
              (.attr "class" "bar")
              (.attr "transform" #(str "translate(" (x-scale (.-x %)) "," (y-scale (.-y %)) ")")))

      ;; new rects
      (-> (.append "rect")
          (.attr "x" 1)
          (.attr "width" (dec (x-scale (.-dx (aget data 0)))))
          (.attr "height" #(- height (y-scale (.-y %)))))

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
        (.text format-label))

    ;; existing rects
    (-> svg
        (.selectAll "rect")
        (.data data)
        (.attr "width" (dec (x-scale (.-dx (aget data 0)))))
        (.attr "height" #(- height (y-scale (.-y %)))))

    (-> svg
        (.select ".x.axis")
        (.call (x-axis x-scale)))))

(defn component [load-test owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (create-chart (om/get-node owner) load-test)
      (update-chart (om/get-node owner) load-test))

    om/IDidUpdate
    (did-update [_ _ _]
      (update-chart (om/get-node owner) load-test))

    om/IRender
    (render [_]
      (let [width 446
            height 150
            top 20
            right 10
            bottom 30
            left 30]
        (dom/div #js {:className "chart response-time-histogram"}
                 (dom/svg #js {:width (+ width left right)
                               :height (+ height top bottom)}
                          (dom/g #js {:transform (str "translate(" left "," top ")")})))))))
