(ns gc-api-browser.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [cljs-http.client :as http]
            [goog.json :as gjson]
            [gc-api-browser.utils :refer [log throttle]]
            [gc-api-browser.store :as store]
            [gc-api-browser.url-bar :as url-bar]
            [gc-api-browser.tabbed-response :as tabbed-response]
            [gc-api-browser.schema-select :as schema-select]
            [gc-api-browser.tabbed-request :as tabbed-request])
  (:import [goog.ui IdGenerator]))

(def default-headers {"Authorization"      "FILL ME IN"
                      "GoCardless-Version" "2015-07-06"
                      "Accept"             "application/json"
                      "Content-Type"       "application/json"})

(def init-app-state
  {:history           []
   :history-id        nil
   :request           {:url     nil
                       :method  "GET"
                       :body    nil
                       :headers default-headers}
   :response          {}
   :selected-resource nil
   :selected-action   nil
   :schema            nil})

(defonce app-state
  (atom init-app-state))

(enable-console-print!)

(defn render-schema-select [app-cursor]
  (dom/div #js {:className "flex-container u-direction-row"}
           (dom/span #js {:className "u-margin-Rm"} "Select a JSON schema")
           (om/build schema-select/component app-cursor)))

(defn render-request-and-response [app]
  (let [{:keys [request response]} app]
    (dom/div #js {:className "flex-container u-align-center u-flex-center"}
             (om/build url-bar/component app)
             (dom/div #js {:className "flex-container u-direction-row request-response"}
                      (om/build tabbed-request/component app)
                      (om/build tabbed-response/component response))
             (render-schema-select app))))

(defn render-init-app [app]
  (if (:downloading-schema app)
    (dom/h1 nil "Downloading schema...")
    (dom/div
      #js {:className "flex-container u-align-center u-flex-center"}
      (dom/header
        #js {:className "header"}
        (dom/h2 #js {:className "header__title u-type-mono"} "Explore"))
      (render-schema-select app))))

(defn fetch-schema! [app]
  (om/update! app :downloading-schema true)
  (go
    (let [schema (async/<! (http/get "https://api.gocardless.com/schema.json"))]
      (om/update! app :downloading-schema false)
      (schema-select/set-schema! app (:body schema)))))

(defn main []
  (let [app-state-chan (async/chan (async/sliding-buffer 1))]
    (om/root
      (fn [app _]
        (reify
          om/IWillMount
          (will-mount [_]
            (js/setTimeout
              (fn []
                (if-let [stored-state (store/read! store/store-key)]
                  (om/update! app stored-state)
                  (fetch-schema! app))
                (store/write-throttled! app-state-chan))))
          om/IRender
          (render [_]
            (if (:schema app)
              (render-request-and-response app)
              (render-init-app app)))))
      app-state
      {:target    (.getElementById js/document "app")
       :tx-listen (fn [_ root-cursor]
                    (async/put! app-state-chan @root-cursor))})))
