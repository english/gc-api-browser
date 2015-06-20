(ns gc-api-browser.tabs
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.headers :as headers]))

(defn edit-body [{:keys [body] :as cursor}]
  (dom/div #js {:className "request-form--field request-form--field__body"}
           (dom/textarea #js {:className "input input--textarea"
                              :style #js {:fontFamily "Monospace"}
                              :value body
                              :onChange #(om/update! cursor :body (.. % -target -value))})))

(defn component [cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [showing-body? (om/get-state owner :showing-body)]
        (dom/div nil
                 (dom/div #js {:className "u-direction-row"}
                          (dom/a #js {:href "#"
                                      :onClick #(om/set-state! owner :showing-body false)} "Headers")
                          (dom/a #js {:href "#"
                                      :onClick #(om/set-state! owner :showing-body true)} "Body"))
                 (dom/div nil
                          (if showing-body?
                            (when (not= "GET" (:method cursor))
                              (edit-body cursor))
                            (om/build headers/component (:headers cursor)))))))))
