(ns load-test-om.form
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events])
  (:import [goog.net XhrIo EventType]))

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

(defn handle-submit [form]
  (let [[resource action] (:selected-resource form)]
    (.log js/console (str "submitting with " resource " and " action))
    (let [xhr (XhrIo.)]
      (events/listen xhr EventType.SUCCESS #(.log js/console "SUCCESS" %))
      (events/listen xhr EventType.ERROR #(.log js/console "ERROR" %))
      (.send xhr (str "http://localhost:3000/run?" resource "&" action)))))

(defn submit-form [form]
  (dom/div #js {:className "load-test-form--field load-test-form--field__button"}
           (dom/div #js {:className "label"} "\u00A0")
           (dom/div #js {:className "btn btn-block"
                         :onClick (partial handle-submit form)}
                    "Start")))

(defn load-test-form [form owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "container"}
               (dom/div #js {:className "main"}
                        (dom/div #js {:className "well load-test-form"}
                                 (endpoint-selection form)
                                 (resource-selection form)
                                 (action-selection form)
                                 (submit-form form)
                                 (dom/div #js {:className "clearfix"})))))))
