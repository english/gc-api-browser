(ns gc-api-browser.response
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn component [resp]
  (reify
    om/IRender
    (render [_]
      (let [json (.stringify js/JSON (clj->js (:body resp)) nil 2)]
        (dom/div #js {:className "well response"}
                 (dom/textarea #js {:readOnly true
                                    :className "input"
                                    :style #js {:fontFamily "Monospace"
                                                :minHeight "245px"}
                                    :value json}))))))
