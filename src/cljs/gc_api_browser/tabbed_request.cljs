(ns gc-api-browser.tabbed-request
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.headers :as headers]))

(defn edit-body [{:keys [body] :as cursor}]
  (dom/div #js {:className "tabbed-request__body"}
           (dom/textarea #js {:className "input input--textarea"
                              :style #js {:fontFamily "Source Code Pro"}
                              :value body
                              :onChange #(om/update! cursor :body (.. % -target -value))})))

(defn component [cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [showing-headers? (om/get-state owner :showing-headers?)]
        (dom/div #js {:className "tabbed-request"}
                 (dom/div #js {:className "tabbed-request__buttons"}
                          (dom/button #js {:onClick #(om/set-state! owner :showing-headers? false)} "Body")
                          (dom/button #js {:onClick #(om/set-state! owner :showing-headers? true)} "Headers"))
                 (dom/div #js {:className "tabbed-request__content"}
                          (if showing-headers?
                            (om/build headers/component (:headers cursor))
                            (when (not= "GET" (:method cursor))
                              (edit-body cursor)))))))))
