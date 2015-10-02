(ns gc-api-browser.url-bar
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.history :as history]
            [gc-api-browser.utils :refer [log]]
            [cljs.core.async :as async]
            [cljs-http.client :as http]
            [gc-api-browser.schema-select :as schema-select]
            [gc-api-browser.json-schema :as json-schema]))

(defn resource-selection [{:keys [selected-resource schema] :as app}]
  (let [resources (sort (json-schema/schema->resources schema))]
    (dom/div #js {:className "url-bar__item url-bar__item--resource select-container"}
             (apply dom/select #js {:className "select-container__select input"
                                    :value     selected-resource
                                    :onChange  (partial schema-select/handle-resource-change app)}
                    (map #(dom/option #js {:value %} %) resources)))))

(defn action-selection [{:keys [selected-resource selected-action schema] :as app}]
  (let [actions (sort (json-schema/resource->actions schema selected-resource))]
    (dom/div #js {:className "url-bar__item url-bar__item--action select-container"}
             (apply dom/select #js {:className "select-container__select input"
                                    :value     (when selected-action (name selected-action))
                                    :onChange  (partial schema-select/handle-action-change app)}
                    (map #(dom/option #js {:value %} %) actions)))))

(defn edit-method [method cursor]
  (dom/div #js {:className "url-bar__item url-bar__item--method select-container"}
           (apply dom/select #js {:className "select-container__select input"
                                  :value     method
                                  :dir       "rtl"
                                  :onChange  #(om/update! cursor :method (.. % -target -value))}
                  (map #(dom/option #js {:value %} %) ["GET" "POST" "PUT"]))))

(defn edit-url [url cursor]
  (dom/div #js {:className "url-bar__item url-bar__item--url"}
           (dom/input #js {:className "input"
                           :value     url
                           :onChange  #(om/update! cursor :url (.. % -target -value))})))

(defn stringify-keys [m]
  (reduce-kv #(assoc %1 (name %2) %3) {} m))

(defn submit-button [cursor submit-chan]
  (dom/div #js {:className "url-bar__item url-bar__item--submit"}
           (dom/button #js {:className "btn"
                            :onClick   #(async/put! submit-chan
                                                    (-> cursor
                                                        (select-keys [:url :method :body :headers])
                                                        (update :headers stringify-keys)))}
                       "Explore")))

(defn handle-response [response app-at-request-time app-map]
  (let [id (random-uuid)
        {:keys [selected-resource selected-action request]} app-at-request-time]
    (-> app-map
        (assoc :response response :history-id id)
        (update :history conj {:request request
                               :response response
                               :id id
                               :selected-resource selected-resource
                               :selected-action selected-action}))))

(defn component [app owner]
  (reify
    om/IInitState
    (init-state [_]
      {:submit-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [submit-chan (om/get-state owner :submit-chan)]
        (go-loop
          []
          (when-some [req (async/<! submit-chan)]
            (let [app-at-request-time @app
                  resp (async/<! (http/request req))]
              (om/transact! app (partial handle-response resp app-at-request-time))
              (recur))))))

    om/IRender
    (render [_]
      (let [submit-chan (om/get-state owner :submit-chan)
            request     (:request app)]
        (dom/div #js {:className "flex-container u-justify-center u-direction-row url-bar"}
                 (history/render-paginator app)
                 (resource-selection app)
                 (action-selection app)
                 (edit-method (:method request) request)
                 (edit-url (:url request) request)
                 (submit-button request submit-chan))))))
