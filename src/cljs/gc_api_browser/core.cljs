(ns gc-api-browser.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [gc-api-browser.request :as request]
            [gc-api-browser.response :as response]))

(def default-headers {"Authorization"      "FILL ME IN"
                      "GoCardless-Version" "2015-04-29"
                      "Accept"             "application/json"
                      "Content-Type"       "application/json"})

(defonce app-state
  (atom {:history []
         :request {:text "Select a JSON schema..."
                   :selected-resource nil
                   :selected-action nil
                   :url nil
                   :method "GET"
                   :body nil
                   :headers {}}}))

(enable-console-print!)

(defn handle-new-response [app resp]
  (om/transact! app (fn [m]
                      (-> m
                          (assoc :response resp)
                          (update :history #(conj % (select-keys m [:request :response])))))))

(defn main []
  (om/root
    (fn [app owner]
      (reify
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
          (dom/div nil
                   (dom/header nil
                               (dom/div #js {:className "container"}
                                        (dom/h2 #js {:id "title"} (get-in app [:request :text]))))
                   (dom/div #js {:className "container"}
                            (dom/div #js {:className "main"}
                                     (dom/div #js {:className "container"}
                                              (dom/div #js {:className "main"}
                                                       (om/build request/component (:request app)
                                                                 {:opts {:handle-new-response-fn (partial handle-new-response app)}})
                                                       (when-let [resp (:response app)]
                                                         (om/build response/component resp))))))))))
    app-state
    {:target (.getElementById js/document "app")}))
