(ns gc-api-browser.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.url-bar :as url-bar]
            [gc-api-browser.tabbed-response :as tabbed-response]
            [gc-api-browser.schema-select :as schema-select]
            [gc-api-browser.tabbed-request :as tabbed-request]))

(def default-headers {"Authorization"      "FILL ME IN"
                      "GoCardless-Version" "2015-04-29"
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
  (swap! app-state (fn [] init-app-state))
  (do
    (.removeItem js/localStorage "schema")
    (.removeItem js/localStorage "headers"))
  (swap! app-state (fn [x] (update-in x [:request] (fn [y] (dissoc y :schema))))))

(defn handle-new-response [app resp]
  (om/transact! app (fn [m]
                      (-> m
                          (assoc :response resp)
                          (update :history #(conj % (select-keys m [:request :response])))))))

(defn render-request-and-response [app]
  [(om/build url-bar/component (:request app)
             {:opts {:handle-new-response-fn (partial handle-new-response app)}})
   (dom/div #js {:className "flex-container u-direction-row request-response"}
            (om/build tabbed-request/component (:request app))
            (om/build tabbed-response/component (:response app)))
   (dom/div nil (schema-select/schema-file (:request app)))])

(defn main []
  (om/root
    (fn [app _]
      (reify
        om/IWillMount
        (will-mount [_]
          ;; Hack! See https://github.com/omcljs/om/issues/336
          (js/setTimeout
            (fn []
              (when-let [json (.getItem js/localStorage "schema")]
                (schema-select/set-schema! (:request app) (.parse js/JSON json))))))
        om/IDidMount
        (did-mount [_]
          ;; Hack! See https://github.com/omcljs/om/issues/336
          (js/setTimeout
            (fn []
              (om/update! app [:request :headers]
                          (or (js->clj (.parse js/JSON (.getItem js/localStorage "headers")))
                              default-headers)))))
        om/IRender
        (render [_]
          (let [schema (get-in app [:request :schema])]
            (apply dom/div #js {:className "flex-container u-flex-center"}
                   (dom/div nil
                            (dom/header #js {:className "header flex-container"}
                                        (dom/h2 #js {:className "header__title u-type-mono"}
                                                (get-in app [:request :text]))))
                   (if schema
                     (render-request-and-response app)
                     (schema-select/schema-file (:request app))))))))
    app-state
    {:target (.getElementById js/document "app")}))
