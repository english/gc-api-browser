(ns gc-api-browser.tabbed-request
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.headers :as headers]))

(defn content-editable [cursor owner]
  (reify om/IRender
    (render [_]
      (dom/code #js {:contentEditable true
                     :dangerouslySetInnerHTML #js {:__html (:body cursor)}
                     :onInput (fn [_]
                                (let [html (.-innerHTML (om/get-node owner))]
                                  (om/update! cursor :body html)))}))))

(defn edit-body [{:keys [body] :as cursor}]
  (dom/pre #js {:className "flex-container tabbed-request__body"}
           (om/build content-editable cursor)))

(defn get? [request]
  (= "GET" (:method request)))

(defn component [request-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [showing-body? (om/get-state owner :showing-body?)]
        (dom/div #js {:className "tabbed-request"}
                 (dom/span nil
                           (if (get? request-cursor)
                             (dom/div #js {:className "tabs"} (dom/button #js {:className "tab-item tab-item--active"} "Headers"))
                             (dom/div #js {:className "tabs"}
                                      (dom/button #js {:className (str "tab-item" (when showing-body? " tab-item--active"))
                                                       :onClick   #(om/set-state! owner :showing-body? true)}
                                                  "Body")
                                      (dom/button #js {:className (str "tab-item" (when-not showing-body? " tab-item--active"))
                                                       :onClick   #(om/set-state! owner :showing-body? false)} "Headers"))))
                 (dom/div nil
                          (if (and showing-body?
                                   (not (get? request-cursor)))
                            (edit-body request-cursor)
                            (om/build headers/component (:headers request-cursor)))))))))
