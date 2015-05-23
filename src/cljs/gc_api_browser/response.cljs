(ns gc-api-browser.response
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.dom-utils :refer [clearfix]]))

(defn render-header [[header-name header-value]]
  (dom/div #js {:className "headers__header"}
           (dom/div #js {:className "headers__header__name"}
                    (dom/input #js {:className "input"
                                    :disabled true
                                    :value header-name}))
           (dom/span #js {:className "headers__header__separator"} ":")
           (dom/div #js {:className "headers__header__value"}
                    (dom/input #js {:className "input"
                                    :disabled true
                                    :value header-value}))))

(defn render-headers [headers]
  (apply dom/div #js {:className "response-field headers"
                      :style #js {:width "50%"
                                  :float "left"}}
         (dom/div #js {:className "label"} "Headers")
         (dom/div #js {:className "headers__header"})
         (map render-header headers)))

(defn render-body [body]
  (let [json-string (.stringify js/JSON (clj->js body) nil 2)]
    (dom/pre #js {:style #js {:width "50%"
                              :float "left"}}
             (dom/code nil json-string))))

(defn component [{:keys [body headers status] :as resp} owner]
  (reify
    om/IRender
    (render [_]
      (let [json-string (.stringify js/JSON (clj->js body) nil 2)]
        (dom/div #js {:className "well response"}
                 (dom/h1 nil "Response"
                         (dom/small #js {:className "response-status"
                                         :style #js {:margin-left "10px"}} status))
                 (render-headers headers)
                 (render-body body)
                 clearfix)))))
