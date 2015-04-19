(ns load-test-client.form
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [load-test-client.schema-select :as schema-select]
            [load-test-client.headers :as headers])
  (:import [goog.net XhrIo EventType]
           [goog json]))

(defn handle-duration-change [form e]
  (om/transact! form #(assoc form :duration (js/parseInt (.. e -target -value)))))

(defn duration-selection [{:keys [duration] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__duration"}
           (dom/div #js {:className "label"} "Duration:")
           (dom/input #js {:className "input" :type "number" :value duration :min "1" :max "20" :step "1"
                           :onChange (partial handle-duration-change form)})))

(defn handle-rate-change [form e]
  (om/transact! form #(assoc form :rate (js/parseInt (.. e -target -value)))))

(defn rate-selection [{:keys [rate] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__rate"}
           (dom/div #js {:className "label"} "Rate:")
           (dom/input #js {:className "input" :type "number" :value rate :min "1" :max "20" :step "1"
                           :onChange (partial handle-rate-change form)})))

(defn handle-submit [{:keys [duration rate headers] :as form} {:keys [http-url] :as api}]
  (doto (XhrIo.)
    (events/listen EventType.SUCCESS #(.log js/console "SUCCESS" %))
    (events/listen EventType.ERROR #(.log js/console "ERROR" %))
    (.send (str http-url "load-tests")
           "POST"
           (.serialize json (clj->js (select-keys form [:url :method :headers :duration :rate])))
           #js {"Content-Type" "application/json"})))

(defn submit-form [form api]
  (dom/div #js {:className "load-test-form--field load-test-form--field__button"}
           (dom/div #js {:className "label"} "\u00A0")
           (dom/div #js {:className "btn btn-block"
                         :onClick (partial handle-submit form api)}
                    "Start")))

(defn edit-url [form]
  (dom/div #js {:className "load-test-form--field load-test-form--field__url"}
           (dom/div #js {:className "label"} "URL")
           (dom/input #js {:className "input"
                           :value (:url form)
                           :onChange #(om/update! form :url (.. % -target -value))})))

(defn edit-method [form]
  (dom/div #js {:className "load-test-form--field load-test-form--field__method"}
           (dom/div #js {:className "label"} "Method")
           (apply dom/select #js {:className "input"
                                  :value (:method form)
                                  :onChange #(om/update! form :method (.. % -target -value))}
                  (map #(dom/option #js {:value %} %) ["GET" "POST" "PUT"]))))

(defn component [{:keys [form api]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "container"}
               (dom/div #js {:className "main"}
                        (dom/div #js {:className "well load-test-form"}
                                 (om/build schema-select/component form)
                                 (dom/div #js {:className "clearfix"})
                                 (edit-url form)
                                 (edit-method form)
                                 (dom/div #js {:className "clearfix"})
                                 (om/build headers/component (:headers form))
                                 (dom/div #js {:className "clearfix"})
                                 (duration-selection form)
                                 (rate-selection form)
                                 (submit-form form api)
                                 (dom/div #js {:className "clearfix"})))))))
