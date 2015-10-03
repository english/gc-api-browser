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
                             (dom/ul
                               nil
                               (dom/li nil (str "message: " (:message error)))
                               (when-not (str/blank? (:data-path error))
                                 (dom/li nil (str "data path: " (:data-path error))))
                               (when-not (str/blank? (:schema-path error))
                                 (dom/li nil (str "schema path: " (:schema-path error)))))))))))))

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
