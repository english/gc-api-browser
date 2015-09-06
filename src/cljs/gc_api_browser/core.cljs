(ns gc-api-browser.core
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cognitect.transit :as transit]
            [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [gc-api-browser.utils :refer [log]]
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

;; handy repl functions
(comment
  (keys @app-state)
  (let [schema (get-in @app-state [:request :schema])
        resource "Bank Details Lookups"]
    (-> (schema-select/schema->resource-node schema resource)
        :links
        first
        :rel))

  (->> (get-in @app-state [:request :schema])
       :definitions
       vals
       first
       keys)
  (keys (get-in @app-state [:request :schema]))
  (swap! app-state (fn [] init-app-state))
  (swap! app-state (fn [x] (update-in x [:request] (fn [y] (dissoc y :schema)))))
  (log (schema-select/schema->action-node (get-in @app-state [:request :schema])
                                          "Customers" "Create a customer")))

(defn handle-new-response [app resp]
  (om/transact! app (fn [m]
                      (-> m
                          (assoc :response resp)
                          (update :history #(conj % (select-keys m [:request :response])))))))

(defn render-request-and-response [app]
  (dom/div #js {:className "flex-container u-align-center u-flex-center"}
           (om/build url-bar/component (:request app)
                     {:opts {:handle-new-response-fn (partial handle-new-response app)}})
           (dom/div #js {:className "flex-container u-direction-row request-response"}
                    (om/build tabbed-request/component (:request app))
                    (om/build tabbed-response/component (:response app)))
           (dom/div nil (schema-select/schema-file (:request app)))))

(defn render-schema-select [app]
  (dom/div #js {:className "flex-container u-align-center u-flex-center"}
           (dom/header #js {:className "header"}
                       (dom/h2 #js {:className "header__title u-type-mono"}
                               (get-in app [:request :text])))
           (dom/div #js {:className "flex-container u-direction-row"}
                    (dom/span #js {:className "u-margin-Rm"} "Select a JSON schema")
                    (schema-select/schema-file (:request app)))))

(defn load-app-state! [app]
  (let [reader (transit/reader :json)]
    (when-let [app-state-str (.getItem js/localStorage "app-state")]
      (om/update! app (transit/read reader app-state-str)))))

; stolen from: https://gist.github.com/swannodette/5886048
(defn throttle [c ms]
  (let [c' (async/chan)]
    (go
      (while true
        (>! c' (<! c))
        (<! (async/timeout ms))))
    c'))

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
                (om/transact! app [:request :headers] #(if (empty? %) default-headers %))))
            (let [writer (transit/writer :json)]
              (go (while true
                    (let [state (async/<! throttled)]
                      (.setItem js/localStorage "app-state" (transit/write writer state)))))))
          om/IRender
          (render [_]
            (if (get-in app [:request :schema])
              (render-request-and-response app)
              (render-schema-select app)))))
      app-state
      {:target (.getElementById js/document "app")
       :tx-listen (fn [_ root-cursor] (async/put! sync-chan @root-cursor))})))
