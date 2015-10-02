(ns gc-api-browser.tabbed-request
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.json :as gjson]
            [gc-api-browser.json-schema :as json-schema]
            [gc-api-browser.headers :as headers]))

(def ENTER 13)

(defn handle-key-down [e]
  (when (= (.-keyCode e) ENTER)
    (.execCommand js/document "insertHTML" false "\n")
    (.preventDefault e)))

(defn content-editable [cursor owner]
  (reify
    om/IRender
    (render [_]
      (dom/code #js {:contentEditable         true
                     :dangerouslySetInnerHTML #js {:__html (:body cursor)}
                     :onKeyDown               handle-key-down
                     :onInput                 (fn [_]
                                                (let [html (.-innerHTML (om/get-node owner))]
                                                  (om/transact! cursor :body (fn [_] html))))}))))

(defn valid-json? [string]
  (try
    (gjson/parse string)
    true
    (catch :default e
      false)))

(defn valid-request? [cursor]
  (let [request-string (get-in cursor [:request :body])
        validation-result (json-schema/validate-request (:schema cursor)
                                                        (:selected-resource cursor)
                                                        (:selected-action cursor)
                                                        request-string)]
    (:valid validation-result)))

(defn edit-body [cursor]
  (dom/pre #js {:className (str "flex-container tabbed-request__body" (when (not (valid-request? cursor)) " error"))}
           (om/build content-editable (:request cursor))))

(defn get? [request]
  (= "GET" (:method request)))

(defn component [app-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [showing-body? (om/get-state owner :showing-body?)
            request-cursor (:request app-cursor)]
        (dom/div #js {:className "tabbed-request"}
                 (dom/span nil
                           (if (get? request-cursor)
                             (dom/div #js {:className "tabs"}
                                      (dom/button #js {:className "tab-item tab-item--active tab-item--only"} "Headers"))
                             (dom/div #js {:className "tabs"}
                                      (dom/button #js {:className (str "tab-item" (when showing-body? " tab-item--active"))
                                                       :onClick   #(om/set-state! owner :showing-body? true)}
                                                  "Body")
                                      (dom/button #js {:className (str "tab-item" (when-not showing-body? " tab-item--active"))
                                                       :onClick   #(om/set-state! owner :showing-body? false)} "Headers"))))
                 (if (and showing-body?
                          (not (get? request-cursor)))
                   (edit-body app-cursor)
                   (om/build headers/component (:headers request-cursor))))))))
