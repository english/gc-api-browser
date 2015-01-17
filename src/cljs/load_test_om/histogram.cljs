(ns load-test-om.histogram
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn x-axis [x-scale]
  (-> (.-svg js/d3)
      .axis
      (.scale x-scale)
      (.orient "bottom")))

(defn get-y-scale [height domain]
  (-> (.. js/d3 -scale linear)
      (.domain (apply array domain))
      (.range #js [height 0])))

(defn get-x-scale [width domain]
  (-> (.. js/d3 -scale linear)
      (.domain (apply array domain))
      (.range #js [0 width])))

(defn create-chart [el load-test]
  (let [width 446
        height 150
        top 20
        right 10
        bottom 30
        left 30

        format-count-fn (.format js/d3 ",.0f")

        values (apply array (map :response-time (:data-points load-test)))

        x-scale (get-x-scale width [0 (apply max values)])

        data-fn (-> (.. js/d3 -layout histogram)
                    (.bins (.ticks x-scale 20)))

        data (data-fn values)

        y-scale (get-y-scale height [0 (.max js/d3 data #(.-y %))])

        svg (-> (.select js/d3 el)
                (.append "svg")
                (.attr "width" (+ width left right))
                (.attr "height" (+ height top bottom))
                (.append "g")
                (.attr "transform" (str "translate(" left "," top ")")))

        bar (-> svg
                (.selectAll ".bar")
                (.data data)
                (.enter)
                (.append "g")
                (.attr "class" "bar")
                (.attr "transform" #(str "translate("
                                         (x-scale (.-x %)) ","
                                         (y-scale (.-y %)) ")")))]

    (-> bar
        (.append "rect")
        (.attr "x" 1)
        (.attr "width" (dec (x-scale (.-dx (aget data 0)))))
        (.attr "height" #(- height (y-scale (.-y %)))))

    (-> bar
        (.append "text")
        (.attr "dy" "0.75em")
        (.attr "y" 6)
        (.attr "x" (/ (x-scale (.-dx (aget data 0))) 2))
        (.attr "text-anchor" "middle")
        (.text #(format-count-fn (.-y %))))

    (-> svg
        (.append "g")
        (.attr "class" "x axis")
        (.attr "transform" (str "translate(0," height ")"))
        (.call (x-axis x-scale)))))

(defn histogram [load-test owner]
  (reify
    om/IDidMount
    (did-mount [_]
      (create-chart (om/get-node owner) load-test))
    om/IRender
    (render [_]
      (dom/div #js {:className "chart response-time-histogram"}))))
