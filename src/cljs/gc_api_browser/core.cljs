(ns gc-api-browser.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.form :as form]))

(defonce app-state
  (atom {:form {:text ""
                :selected-resource nil :selected-action nil
                :url nil :method "GET" :body nil
                :headers {"GoCardless-Version" "2014-11-03"
                          "Accept" "application/json"
                          "Content-Type" "application/json"}}}))

(enable-console-print!)

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IRender
        (render [_]
          (dom/div nil
                   (dom/header nil
                               (dom/div #js {:className "container"}
                                        (dom/h2 #js {:id "title"} (:text (:form app)))))
                   (dom/div #js {:className "container"}
                            (dom/div #js {:className "main"}
                                     (om/build form/component app)))))))
    app-state
    {:target (. js/document (getElementById "app"))}))
