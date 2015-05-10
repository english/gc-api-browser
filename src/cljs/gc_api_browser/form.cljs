(ns gc-api-browser.form
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [goog.events :as events]
            [cljs-http.client :as http]
            [gc-api-browser.schema-select :as schema-select]
            [gc-api-browser.headers :as headers]
            [gc-api-browser.response :as response])
  (:import [goog.net XhrIo EventType]
           [goog json]))

(defn submit-form [form owner]
  (dom/div #js {:className "request-form--field request-form--field__button"}
           (dom/div #js {:className "label"} "\u00A0")
           (dom/div #js {:className "btn btn-block"
                         :onClick #(async/put! (om/get-state owner :submit-chan)
                                               (select-keys form [:url :method :body :headers]))}
                    "Send")))

(defn edit-url [form]
  (dom/div #js {:className "request-form--field request-form--field__url"}
           (dom/div #js {:className "label"} "URL")
           (dom/input #js {:className "input"
                           :value (:url form)
                           :onChange #(om/update! form :url (.. % -target -value))})))

(defn edit-method [form]
  (dom/div #js {:className "request-form--field request-form--field__method"}
           (dom/div #js {:className "label"} "Method")
           (apply dom/select #js {:className "input"
                                  :value (:method form)
                                  :onChange #(om/update! form :method (.. % -target -value))}
                  (map #(dom/option #js {:value %} %) ["GET" "POST" "PUT"]))))

(defn edit-body [form]
  (dom/div #js {:className "request-form--field request-form--field__body"}
           (dom/div #js {:className "label"} "Body")
           (dom/textarea #js {:className "input"
                              :style #js {:fontFamily "Monospace"
                                          :minHeight "245px"}
                              :value (:body form)
                              :onChange #(om/update! form :body (.. % -target -value))})))

(defn component [{:keys [form]} owner]
  (reify
    om/IInitState
    (init-state [_]
      {:submit-chan (async/chan)
       :response-chan (async/chan)})

    om/IWillMount
    (will-mount [_]
      (let [submit-chan (om/get-state owner :submit-chan)
            response-chan (om/get-state owner :response-chan)]
        (go (loop []
              (let [request (async/<! submit-chan)]
                (async/pipe (http/request request) response-chan false)
                (recur))))
        (go (loop []
              (let [resp (async/<! response-chan)]
                (om/update! form :response resp)
                (recur))))))

    om/IRender
    (render [_]
      (dom/div #js {:className "container"}
               (dom/div #js {:className "main"}
                        (dom/div #js {:className "well request-form"}
                                 (om/build schema-select/component form)
                                 (dom/div #js {:className "clearfix"})
                                 (edit-url form)
                                 (edit-method form)
                                 (dom/div #js {:className "clearfix"})
                                 (om/build headers/component (:headers form))
                                 (when (not= "GET" (:method form)) (edit-body form))
                                 (dom/div #js {:className "clearfix"})
                                 (submit-form form owner)
                                 (dom/div #js {:className "clearfix"}))
                        (dom/div #js {:className "well response"}
                                 (when (:response form)
                                   (om/build response/component (:response form)))))))))
