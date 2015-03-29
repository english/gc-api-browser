(ns load-test-client.bar
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn component [bar x-scale y-scale height]
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
                         (when (pos? (.-y bar)) ;; don't show '0's
                           (.-y bar))))))))
