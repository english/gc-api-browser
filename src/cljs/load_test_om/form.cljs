(ns load-test-om.form
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events])
  (:import [goog.net XhrIo EventType]
           [goog json]))

(defn handle-input-change [form e]
  (om/transact! form [:selected-resource 0] #(.. e -target -value))
  (om/transact! form [:selected-resource 1]
                #(first (get (:resources form)
                             (first (:selected-resource form))))))

(defn handle-action-change [form e]
  (om/transact! form [:selected-resource 1] #(.. e -target -value)))

(defn endpoint-selection [{:keys [url]}]
  (dom/div
    #js {:className "load-test-form--field load-test-form--field__endpoint"}
    (dom/div #js {:className "label"} "Endpoint:")
    (dom/input #js {:type "text"
                    :value url
                    :className "input"
                    :disabled "true"})))

(defn resource-selection [{:keys [selected-resource resources] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__resource"}
           (dom/div #js {:className "label"} "Resource:")
           (apply dom/select #js {:className "input"
                                  :value (first (:selected-resource form))
                                  :onChange (partial handle-input-change form)}
                  (map #(dom/option #js {:value %} %)
                       (keys (:resources form))))))

(defn action-selection [{:keys [selected-resource resources] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__action"}
           (dom/div #js {:className "label"} "Action:")
           (apply dom/select #js {:className "input"
                                  :value (second selected-resource)
                                  :onChange (partial handle-action-change form)}
                  (map #(dom/option #js {:value %} %)
                       (get resources (first selected-resource))))))

(defn handle-duration-change [form e]
  (om/transact! form #(assoc form :duration (.. e -target -value))))

(defn duration-selection [{:keys [duration] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__duration"}
           (dom/div #js {:className "label"} "Duration:")
           (dom/input #js {:className "input" :type "number" :value duration :min "1" :max "20" :step "1"
                           :onChange (partial handle-duration-change form)})))

(defn handle-rate-change [form e]
  (om/transact! form #(assoc form :rate (.. e -target -value))))

(defn rate-selection [{:keys [rate] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__rate"}
           (dom/div #js {:className "label"} "rate:")
           (dom/input #js {:className "input" :type "number" :value rate :min "1" :max "20" :step "1"
                           :onChange (partial handle-rate-change form)})))

(defn handle-submit [{:keys [duration rate] :as form}]
  (let [[resource action] (:selected-resource form)
        duration (js/parseInt duration)
        rate (js/parseInt rate)]
    (doto (XhrIo.)
      (events/listen EventType.SUCCESS #(.log js/console "SUCCESS" %))
      (events/listen EventType.ERROR #(.log js/console "ERROR" %))
      (.send "http://localhost:3000/load-tests"
             "POST"
             (.serialize json (clj->js {:resource resource :action action :duration duration :rate rate}))
             #js {"Content-Type" "application/json"}))))

(defn submit-form [form]
  (dom/div #js {:className "load-test-form--field load-test-form--field__button"}
           (dom/div #js {:className "label"} "\u00A0")
           (dom/div #js {:className "btn btn-block"
                         :onClick (partial handle-submit form)}
                    "Start")))

(defn component [form owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "container"}
               (dom/div #js {:className "main"}
                        (dom/div #js {:className "well load-test-form"}
                                 (endpoint-selection form)
                                 (resource-selection form)
                                 (action-selection form)
                                 (duration-selection form)
                                 (rate-selection form)
                                 (submit-form form)
                                 (dom/div #js {:className "clearfix"})))))))
