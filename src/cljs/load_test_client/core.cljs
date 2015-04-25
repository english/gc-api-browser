(ns load-test-client.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.repl :as repl :include-macros true]
            [goog.events :as events]
            [goog.json :as gjson]
            [goog.net.XhrIo :as gxhr]
            [load-test-client.form :as form]
            [load-test-client.load-tests :as load-tests])
  (:import [goog.net XhrIo WebSocket]
           [goog.ui IdGenerator]))

(defonce app-state
  (atom {:api {:http-url "http://localhost:3000/"
               :ws-url "ws://localhost:3000/"}
         :form {:text ""
                :duration 5 :rate 3 :selected-resource nil :selected-action nil
                :url nil :method "GET" :body nil :headers {}}
         :load-tests {}}))

(enable-console-print!)

(defn handle-new-or-updated-load-test [app data]
  (om/transact! app :load-tests
                #(merge % (js->clj (gjson/parse data) :keywordize-keys true))))

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IWillMount
        (will-mount [_]
          (let [ws (WebSocket.)
                ws-endpoint (str (-> app :api :ws-url) "load-tests")]
            (doto ws
              (events/listen WebSocket.EventType.MESSAGE #(handle-new-or-updated-load-test app (.-message %)))
              (.open ws-endpoint))
            (om/set-state! owner :load-tests-ws ws)))

        om/IWillUnmount
        (will-unmount [_]
          (.close (om/get-state owner :load-tests-ws)))

        om/IRender
        (render [_]
          (dom/div nil
                   (dom/header nil
                               (dom/div #js {:className "container"}
                                        (dom/h2 #js {:id "title"} (:text (:form app)))))
                   (dom/div #js {:className "container"}
                            (dom/div #js {:className "main"}
                                     (om/build form/component app)
                                     (dom/div #js {:className "hr"})
                                     (om/build load-tests/component (:load-tests app))))))))
    app-state
    {:target (. js/document (getElementById "app"))}))
