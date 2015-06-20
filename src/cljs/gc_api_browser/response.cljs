(ns gc-api-browser.response
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.dom-utils :refer [clearfix]]))

(defn render-header [[header-name header-value]]
  (dom/div #js {:className "headers__header u-direction-row"}
           (dom/div #js {:className "headers__header__name u-margin-Rxxs"}
                    (dom/input #js {:className "input"
                                    :disabled true
                                    :value header-name}))
           (dom/div #js {:className "headers__header__value u-margin-Rxxs"}
                    (dom/input #js {:className "input"
                                    :disabled true
                                    :value header-value}))))

(defn render-headers [headers]
  (apply dom/div #js {:className "response-field headers"}
         (map render-header headers)))

(defn render-body [body]
  (let [json-string (.stringify js/JSON (clj->js body) nil 2)]
    (dom/pre #js {:style #js {:width "50%" :float "left"}}
             (dom/code nil json-string))))

(defn component [cursor owner]
  (reify
    om/IRender
    (render [_]
      (if (empty? cursor)
        (dom/div nil)
        (let [showing-headers? (om/get-state owner :showing-headers?)]
          (dom/div #js {:className "response"}
                   (dom/div nil
                            (dom/button #js {:onClick #(om/set-state! owner :showing-headers? true)} "Body")
                            (dom/button #js {:onClick #(om/set-state! owner :showing-headers? false)} "Headers"))
                   (if showing-headers?
                     (render-headers (:headers cursor))
                     (render-body (:body cursor)))))))))
