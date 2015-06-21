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

(defn get? [request]
  (= "GET" (:method request)))

(defn component [request-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [showing-body? (om/get-state owner :showing-body?)]
        (dom/div #js {:className "tabbed-request"}
                 (dom/div #js {:className "tabbed-request__buttons u-direction-row"}
                          (when-not (get? request-cursor)
                            (dom/span #js {:className "tabbed-request__button"}
                                      (dom/button #js {:className "btn"
                                                       :onClick   #(om/set-state! owner :showing-body? true)} "Body")))
                          (dom/span #js {:className "tabbed-request__button"}
                                    (dom/button #js {:className "btn"
                                                     :onClick   #(om/set-state! owner :showing-body? false)} "Headers")))
                 (dom/div #js {:className "tabbed-request__content"}
                          (if (and showing-body?
                                   (not (get? request-cursor)))
                            (edit-body request-cursor)
                            (om/build headers/component (:headers request-cursor)))))))))