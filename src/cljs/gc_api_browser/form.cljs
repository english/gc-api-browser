(ns gc-api-browser.form
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [gc-api-browser.schema-select :as schema-select]
            [gc-api-browser.headers :as headers])
  (:import [goog.net XhrIo EventType]
           [goog json]))

(defn handle-submit [form api]
  (doto (XhrIo.)
    (events/listen EventType.SUCCESS #(.log js/console "SUCCESS" %))
    (events/listen EventType.ERROR #(.log js/console "ERROR" %))
    (.send (str "the-url") ;; FIXME
           "POST"
           (.serialize json (clj->js (select-keys form [:url :method :headers :body])))
           #js {"Content-Type" "application/json"})))

(defn submit-form [form api]
  (dom/div #js {:className "request-form--field request-form--field__button"}
           (dom/div #js {:className "label"} "\u00A0")
           (dom/div #js {:className "btn btn-block"
                         :onClick (partial handle-submit form api)}
                    "Start")))

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
                              :value (:body form)
                              :onChange #(om/update! form :body (.. % -target -value))})))

(defn component [{:keys [form api]} owner]
  (reify
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
                                 (submit-form form api)
                                 (dom/div #js {:className "clearfix"})))))))
