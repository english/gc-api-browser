(ns gc-api-browser.tabbed-request
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as str]
            [goog.json :as gjson]
            [gc-api-browser.utils :refer [log]]
            [gc-api-browser.json-schema :as json-schema]
            [gc-api-browser.headers :as headers]))

(def ENTER 13)

(defn handle-paste [e]
  (let [text (.getData (.-clipboardData e) "text/plain")]
    (.execCommand js/document "insertHTML", false text))
  (.preventDefault e))

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
                     :className               "tabbed-request__content"
                     :data-placeholder        "Type JSON here..."
                     :onKeyDown               handle-key-down
                     :onPaste                 handle-paste
                     :onInput                 (fn [_]
                                                (let [html (.-innerHTML (om/get-node owner))]
                                                  (om/transact! cursor :body (fn [_] html))))}))))

(defn validate-request [cursor]
  (let [{:keys [schema selected-resource selected-action request]} cursor
        request-string (:body request)]
    (json-schema/validate-request schema selected-resource selected-action request-string)))

(defn edit-body [cursor]
  (let [validation-result (validate-request cursor)
        class-name (str "flex-container tabbed-request__body"
                        (when (not (:valid validation-result)) " error"))]
    (dom/div nil
             (dom/pre #js {:className class-name}
                      (om/build content-editable (:request cursor)))
             (when (seq (:errors validation-result))
               (dom/div
                 nil
                 "Errors:"
                 (dom/ul nil
                         (for [error (:errors validation-result)]
                           (dom/li
                             nil
                             (:message error)
                             (dom/ul
                               nil
                               (when-not (str/blank? (:data-path error))
                                 (dom/li nil (str "Data path: " (:data-path error))))
                               (when-not (str/blank? (:schema-path error))
                                 (dom/li nil (str "Schema path: " (:schema-path error)))))))))))))

(defn get? [request]
  (= "GET" (:method request)))

(defn component [app-cursor owner]
  (reify
    om/IRender
    (render [_]
      (let [{:keys [showing-request-body?]} app-cursor
            request-cursor (:request app-cursor)]
        (dom/div #js {:className "tabbed-request"}
                 (dom/h2 #js {:className "tabbed-request__header"} "Request")
                 (dom/div #js {:className "tabbed-request__inner"}
                          (dom/span
                            nil
                            (dom/div #js {:className "tabs"}
                                     (dom/button #js {:className (str "tab-item" (when showing-request-body? " tab-item--active"))
                                                      :onClick   #(om/update! app-cursor :showing-request-body? true)
                                                      :disabled  (get? request-cursor)}
                                                 "Body")
                                     (dom/button #js {:className (str "tab-item" (when-not showing-request-body? " tab-item--active"))
                                                      :onClick   #(om/update! app-cursor :showing-request-body? false)} "Headers")))
                          (if (and showing-request-body?
                                   (not (get? request-cursor)))
                            (edit-body app-cursor)
                            (om/build headers/component (:headers request-cursor)))))))))
