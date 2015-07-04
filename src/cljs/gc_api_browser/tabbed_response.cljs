(ns gc-api-browser.tabbed-response
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.dom-utils :refer [clearfix]]))

(defn render-header [[header-name header-value]]
  (dom/div #js {:className "flex-container headers__header u-direction-row"}
           (dom/div #js {:className "flex-item headers__header__name u-margin-Rxxs"}
                    (dom/input #js {:className "input"
                                    :disabled  true
                                    :value     header-name}))
           (dom/div #js {:className "flex-item headers__header__value u-margin-Rxxs"}
                    (dom/input #js {:className "input"
                                    :disabled  true
                                    :value     header-value}))))

(defn render-headers [headers]
  (apply dom/div #js {:className "tabbed-response__headers headers"}
         (map render-header headers)))

(defn render-body [body]
  (let [json-string (.stringify js/JSON (clj->js body) nil 2)]
    (dom/div #js {:className "tabbed-response__body"}
             (dom/pre nil (dom/code nil json-string)))))

(defn component [cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [showing-headers? (om/get-state owner :showing-headers?)]
        (dom/div #js {:className "tabbed-response"}
                 (when-not (empty? cursor)
                   (dom/div nil
                            (dom/div #js {:className "tabbed-response__buttons u-direction-row"}
                                     (dom/button #js {:className (str "tab-item" (when-not showing-headers? " tab-item--active"))
                                                      :onClick #(om/set-state! owner :showing-headers? false)}
                                                 "Body")
                                     (dom/button #js {:className (str "tab-item" (when showing-headers? " tab-item--active"))
                                                      :onClick #(om/set-state! owner :showing-headers? true)}
                                                 "Headers"))
                            (dom/div #js {:className "tabbed-response__content"})
                            (if showing-headers?
                              (render-headers (:headers cursor))
                              (render-body (:body cursor))))))))))
