(ns gc-api-browser.url-bar
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [cljs-http.client :as http]
            [gc-api-browser.schema-select :as schema-select]))

(defn resource-selection [{:keys [selected-resource schema] :as request}]
  (dom/div #js {:className "url-bar__item select-container"}
           (apply dom/select #js {:className "select-container__select input"
                                  :value selected-resource
                                  :onChange (partial schema-select/handle-resource-change request)}
                  (map #(dom/option #js {:value %} %)
                       (schema-select/schema->resources schema)))))

(defn action-selection [{:keys [selected-resource selected-action schema] :as request}]
  (dom/div #js {:className "url-bar__item select-container"}
           (apply dom/select #js {:className "select-container__select input"
                                  :value (when selected-action (name selected-action))
                                  :onChange (partial schema-select/handle-action-change request)}
                  (map #(dom/option #js {:value %} %)
                       (schema-select/resource->actions schema selected-resource)))))

(defn edit-method [{:keys [method] :as cursor}]
  (dom/div #js {:className "url-bar__item url-bar__item--method select-container"}
           (apply dom/select #js {:className "select-container__select input"
                                  :value     method
                                  :dir       "rtl"
                                  :onChange  #(om/update! cursor :method (.. % -target -value))}
                  (map #(dom/option #js {:value %} %) ["GET" "POST" "PUT"]))))

(defn edit-url [{:keys [url] :as cursor}]
  (dom/div nil
           (dom/input #js {:className "input url-bar__item url-bar__item--url"
                           :value     url
                           :onChange  #(om/update! cursor :url (.. % -target -value))})))

(defn submit-button [cursor owner]
  (let [submit-chan (om/get-state owner :submit-chan)]
    (dom/div #js {:className "url-bar__submit"}
             (dom/button #js {:className "btn"
                              :onClick   #(async/put! submit-chan
                                                      (select-keys cursor [:url :method :body :headers]))}
                         "Send"))))

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
               (dom/div #js {:className "u-direction-row u-flex-none"}
                        (resource-selection cursor)
                        (action-selection cursor)
                        (edit-method cursor)
                        (edit-url cursor)
                        (submit-button cursor owner))))))
