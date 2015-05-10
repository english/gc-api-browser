(ns gc-api-browser.request
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.core.async :as async]
            [goog.events :as events]
            [cljs-http.client :as http]
            [gc-api-browser.schema-select :as schema-select]
            [gc-api-browser.headers :as headers]
            [gc-api-browser.dom-utils :refer [clearfix]])
  (:import [goog.net XhrIo EventType]
           [goog json]))

(defn submit [cursor owner]
  (dom/div #js {:className "request-form--field request-form--field__button"}
           (dom/div #js {:className "label"} "\u00A0")
           (dom/div #js {:className "btn btn-block"
                         :onClick #(async/put! (om/get-state owner :submit-chan)
                                               (select-keys cursor [:url :method :body :headers]))}
                    "Send")))

(defn edit-url [{:keys [url] :as cursor}]
  (dom/div #js {:className "request-form--field request-form--field__url"}
           (dom/div #js {:className "label"} "URL")
           (dom/input #js {:className "input"
                           :value url
                           :onChange #(om/update! cursor :url (.. % -target -value))})))

(defn edit-method [{:keys [method] :as cursor}]
  (dom/div #js {:className "request-form--field request-form--field__method"}
           (dom/div #js {:className "label"} "Method")
           (apply dom/select #js {:className "input"
                                  :value method
                                  :onChange #(om/update! cursor :method (.. % -target -value))}
                  (map #(dom/option #js {:value %} %) ["GET" "POST" "PUT"]))))

(defn edit-body [{:keys [body] :as cursor}]
  (dom/div #js {:className "request-form--field request-form--field__body"}
           (dom/div #js {:className "label"} "Body")
           (dom/textarea #js {:className "input"
                              :style #js {:fontFamily "Monospace"
                                          :minHeight "245px"}
                              :value body
                              :onChange #(om/update! cursor :body (.. % -target -value))})))

(defn component [cursor owner {:keys [handle-new-response-fn] :as opts}]
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
              (when-let [req (async/<! submit-chan)]
                (let [resp (async/<! (http/request req))]
                  (handle-new-response-fn resp)
                  (recur)))))))

    om/IRender
    (render [_]
      (let [{:keys [headers method]} cursor]
        (dom/div #js {:className "well request-form"}
                 (om/build schema-select/component cursor)
                 clearfix
                 (edit-url cursor)
                 (edit-method cursor)
                 clearfix
                 (om/build headers/component headers)
                 (when (not= "GET" method)
                   (edit-body cursor))
                 clearfix
                 (submit cursor owner)
                 clearfix)))))
