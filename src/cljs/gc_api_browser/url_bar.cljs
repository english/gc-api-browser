(ns gc-api-browser.url-bar
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [cljs-http.client :as http]
            [gc-api-browser.schema-select :as schema-select]))

(defn resource-selection [{:keys [selected-resource schema] :as request}]
  (let [resources (sort (schema-select/schema->resources schema))]
    (dom/div #js {:className "url-bar__item url-bar__item--resource select-container"}
             (apply dom/select #js {:className "select-container__select input"
                                    :value     selected-resource
                                    :onChange  (partial schema-select/handle-resource-change request)}
                    (map #(dom/option #js {:value %} %) resources)))))

(defn action-selection [{:keys [selected-resource selected-action schema] :as request}]
  (let [actions (sort (schema-select/resource->actions schema selected-resource))]
    (dom/div #js {:className "url-bar__item url-bar__item--action select-container"}
             (apply dom/select #js {:className "select-container__select input"
                                    :value     (when selected-action (name selected-action))
                                    :onChange  (partial schema-select/handle-action-change request)}
                    (map #(dom/option #js {:value %} %) actions)))))

(defn edit-method [{:keys [method] :as cursor}]
  (dom/div #js {:className "url-bar__item url-bar__item--method select-container"}
           (apply dom/select #js {:className "select-container__select input"
                                  :value     method
                                  :dir       "rtl"
                                  :onChange  #(om/update! cursor :method (.. % -target -value))}
                  (map #(dom/option #js {:value %} %) ["GET" "POST" "PUT"]))))

(defn edit-url [{:keys [url] :as cursor}]
  (dom/div #js {:className "u-flex2"}
           (dom/input #js {:className "input url-bar__item url-bar__item--url"
                           :value     url
                           :onChange  #(om/update! cursor :url (.. % -target -value))})))

(defn stringify-keys [m]
  (reduce-kv #(assoc %1 (name %2) %3) {} m))

(defn submit-button [cursor owner]
  (let [submit-chan (om/get-state owner :submit-chan)]
    (dom/div #js {:className "url-bar__item url-bar__item--submit"}
             (dom/button #js {:className "btn"
                              :onClick   #(async/put! submit-chan
                                                      (-> cursor
                                                          (select-keys [:url :method :body :headers])
                                                          (update :headers stringify-keys)))}
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
      (dom/div #js {:className "flex-container u-justify-center u-direction-row url-bar"}
               (resource-selection cursor)
               (action-selection cursor)
               (edit-method cursor)
               (edit-url cursor)
               (submit-button cursor owner)))))
