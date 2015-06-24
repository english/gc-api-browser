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
                 (dom/div #js {:className "tabs" :style #js {:justifyContent "center"}}
                          (when-not (get? request-cursor)
                            (dom/button #js {:className (str "tab-item" (when showing-body? " tab-item--active"))
                                             :onClick   #(om/set-state! owner :showing-body? true)}
                                        "Body"))
                          (dom/button #js {:className (str "tab-item" (when-not showing-body? " tab-item--active"))
                                           :onClick   #(om/set-state! owner :showing-body? false)} "Headers"))
                 (dom/div nil
                          (if (and showing-body?
                                   (not (get? request-cursor)))
                            (edit-body request-cursor)
                            (om/build headers/component (:headers request-cursor)))))))))
