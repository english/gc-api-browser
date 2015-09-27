(ns gc-api-browser.core
  (:require-macros [cljs.core.async.macros :refer [go-loop]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
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
   :text              "Explore"
   :selected-resource nil
   :selected-action   nil
   :schema            nil})

(defonce app-state
  (atom init-app-state))

(enable-console-print!)

(defn guid []
  (.getNextUniqueId (.getInstance IdGenerator)))

(defn handle-response [request response app-cursor]
  (let [id (guid)]
    (-> app-cursor
        (assoc :response response :history-id id)
        (update :history conj {:request request
                               :response response
                               :id id}))))

(defn render-schema-select [app-cursor]
  (dom/div #js {:className "flex-container u-direction-row"}
           (dom/span #js {:className "u-margin-Rm"} "Select a JSON schema")
           (om/build schema-select/component app-cursor)))

(defn at-start? [history id]
  (= id (:id (first history))))

(defn can-go-back? [{:keys [history history-id]}]
  (if history-id
    (not (at-start? history history-id))
    (seq history)))

(defn go-back! [app]
  (let [entry (deref (if-let [id (:history-id app)]
                       (last (take-while #(not= id (:id %)) (:history app)))
                       (last (:history app))))]
    (om/transact!
      app
      (fn [m]
        (assoc m :request (:request entry)
                 :response (:response entry)
                 :history-id (:id entry))))))

(defn can-go-forward? [{:keys [history-id history]}]
  (and history-id (not= history-id (:id (last history)))))

(defn go-forward! [app]
  (let [entry (deref (second (drop-while #(not= (:history-id app) (:id %)) (:history app))))]
    (om/transact!
      app
      (fn [m]
        (assoc m :request (:request entry)
                 :response (:response entry)
                 :history-id (:id entry))))))

(defn render-paginator [app]
  (dom/div #js {:className "flex-container u-direction-row u-justify-center u-margin-Bm"
                :style #js {:flex "1 100%"}}
           (dom/button #js {:onClick #(go-back! app)
                            :disabled (not (can-go-back? app))} "<")
           (dom/button #js {:onClick #(go-forward! app)
                            :disabled (not (can-go-forward? app))} ">")))

(defn render-request-and-response [app]
  (let [{:keys [request response]} app]
    (dom/div #js {:className "flex-container u-align-center u-flex-center"}
             (om/build url-bar/component app
                       {:opts {:handle-new-response-fn #(om/transact! app (partial handle-response %1 %2))}})
             (dom/div #js {:className "flex-container u-direction-row request-response"}
                      (om/build tabbed-request/component request)
                      (om/build tabbed-response/component response))
             (render-paginator app)
             (render-schema-select app))))

(defn render-init-app [app]
  (dom/div #js {:className "flex-container u-align-center u-flex-center"}
           (dom/header #js {:className "header"}
                       (dom/h2 #js {:className "header__title u-type-mono"} (:text app)))
           (render-schema-select app)))

(defn load-app-state! [app]
  (when-let [stored-state (store/read! store/store-key)]
    (om/update! app stored-state)))

(defn sync-app-state! [c]
  (let [throttled (throttle c 300)]
    (go-loop []
             (when-some [state (async/<! throttled)]
               (store/write! store/store-key state)
               (recur)))))

(defn main []
  (let [app-state-chan (async/chan (async/sliding-buffer 1))]
    (om/root
      (fn [app _]
        (reify
          om/IWillMount
          (will-mount [_]
            (js/setTimeout
              (fn []
                (load-app-state! app)
                (sync-app-state! app-state-chan))))
          om/IRender
          (render [_]
            (if (:schema app)
              (render-request-and-response app)
              (render-init-app app)))))
      app-state
      {:target    (.getElementById js/document "app")
       :tx-listen (fn [_ root-cursor]
                    (async/put! app-state-chan @root-cursor))})))
