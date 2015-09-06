(ns gc-api-browser.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cognitect.transit :as transit]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [gc-api-browser.utils :refer [log throttle]]
            [gc-api-browser.store :as store]
            [gc-api-browser.url-bar :as url-bar]
            [gc-api-browser.tabbed-response :as tabbed-response]
            [gc-api-browser.schema-select :as schema-select]
            [gc-api-browser.tabbed-request :as tabbed-request]))

(def default-headers {"Authorization"      "FILL ME IN"
                      "GoCardless-Version" "2015-07-06"
                      "Accept"             "application/json"
                      "Content-Type"       "application/json"})

(def init-app-state
  {:history  []
   :request  {:text              "Restman"
              :selected-resource nil
              :selected-action   nil
              :url               nil
              :method            "GET"
              :body              nil
              :headers           {}}
   :response {}})

(defonce app-state
  (atom init-app-state))

(enable-console-print!)

(defn handle-new-response [app resp]
  (om/transact! app (fn [m]
                      (-> m
                          (assoc :response resp)
                          (update :history #(conj % (select-keys m [:request :response])))))))

(defn render-request-and-response [app]
  (let [{:keys [request response]} app]
    (dom/div #js {:className "flex-container u-align-center u-flex-center"}
             (om/build url-bar/component request
                       {:opts {:handle-new-response-fn (partial handle-new-response app)}})
             (dom/div #js {:className "flex-container u-direction-row request-response"}
                      (om/build tabbed-request/component request)
                      (om/build tabbed-response/component response))
             (dom/div nil (schema-select/schema-file request)))))

(defn render-schema-select [app]
  (dom/div #js {:className "flex-container u-align-center u-flex-center"}
           (dom/header #js {:className "header"}
                       (dom/h2 #js {:className "header__title u-type-mono"}
                               (get-in app [:request :text])))
           (dom/div #js {:className "flex-container u-direction-row"}
                    (dom/span #js {:className "u-margin-Rm"} "Select a JSON schema")
                    (schema-select/schema-file (:request app)))))

(defn load-app-state! [app]
  (when-let [stored-state (store/read!)]
    (om/update! app stored-state)))

(defn set-default-headers! [app]
  (om/transact! app [:request :headers]
                (fn [headers] (if (empty? headers) default-headers headers))))

(defn tx-listener [sync-chan _ root-cursor]
  (async/put! sync-chan @root-cursor))

(defn main []
  (let [sync-chan (async/chan (async/sliding-buffer 1))
        throttled (throttle sync-chan 300)]
    (om/root
      (fn [app _]
        (reify
          om/IWillMount
          (will-mount [_]
            (js/setTimeout
              (fn []
                (load-app-state! app)
                (set-default-headers! app)))
            (go (while true
                  (let [state (async/<! throttled)]
                    (store/write! state)))))
          om/IRender
          (render [_]
            (if (get-in app [:request :schema])
              (render-request-and-response app)
              (render-schema-select app)))))
      app-state
      {:target (.getElementById js/document "app")
       :tx-listen (partial tx-listener sync-chan)})))
