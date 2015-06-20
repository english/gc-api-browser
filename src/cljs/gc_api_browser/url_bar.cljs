(ns gc-api-browser.url-bar
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [cljs-http.client :as http]
            [gc-api-browser.schema-select :as schema-select]
            [gc-api-browser.dom-utils :refer [clearfix]]))

(defn submit [cursor owner]
  (dom/div #js {:className ""
                :onClick   #(async/put! (om/get-state owner :submit-chan)
                                        (select-keys cursor [:url :method :body :headers]))}
           "Send"))

(defn edit-url [{:keys [url] :as cursor}]
  (dom/div nil
           (dom/input #js {:className "url-bar__url"
                           :value url
                           :onChange #(om/update! cursor :url (.. % -target -value))})))

(defn edit-method [{:keys [method] :as cursor}]
  (dom/div nil
           (apply dom/select #js {:className "input"
                                  :value method
                                  :onChange #(om/update! cursor :method (.. % -target -value))}
                  (map #(dom/option #js {:value %} %) ["GET" "POST" "PUT"]))))

(defn component [cursor owner {:keys [handle-new-response-fn] :as opts}]
  (reify
    om/IInitState
    (init-state [_]
      {:submit-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [submit-chan (om/get-state owner :submit-chan)]
        (go (loop []
              (when-let [req (async/<! submit-chan)]
                (let [resp (async/<! (http/request req))]
                  (handle-new-response-fn resp)
                  (recur)))))))

    om/IRender
    (render [_]
      (dom/div #js {:className "url-bar"}
               (dom/div #js {:className "u-direction-row"}
                        (schema-select/resource-selection cursor)
                        (schema-select/action-selection cursor)
                        (edit-method cursor)
                        (edit-url cursor)
                        (submit cursor owner))))))
