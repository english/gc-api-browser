(ns load-test-client.form
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.events :as events]
            [load-test-client.schema-select :as schema-select])
  (:import [goog.net XhrIo EventType]
           [goog json]))

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
           (dom/div #js {:className "label"} "Rate:")
           (dom/input #js {:className "input" :type "number" :value rate :min "1" :max "20" :step "1"
                           :onChange (partial handle-rate-change form)})))

(defn handle-submit [{:keys [request duration rate] :as form} {:keys [http-url] :as api}]
  (let [duration (js/parseInt duration)
        rate (js/parseInt rate)]
    (doto (XhrIo.)
      (events/listen EventType.SUCCESS #(.log js/console "SUCCESS" %))
      (events/listen EventType.ERROR #(.log js/console "ERROR" %))
      (.send (str http-url "load-tests")
             "POST"
             (.serialize json (clj->js {:method (:method request)
                                        :url (:url request)
                                        :duration duration
                                        :rate rate}))
             #js {"Content-Type" "application/json"}))))

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

(defn edit-headers [form]
  (dom/div #js {:className "load-test-form--field laod-test-from--field__headers"}
           (apply dom/div #js {:className "label"} "Headers"
                  (dom/input #js {:className "input"
                                  :value (:new-header form)
                                  :onChange #(om/update! form :new-header (.. % -target -value))})
                  (dom/input #js {:className "input"
                                  :value (:new-header-value form)
                                  :onChange #(om/update! form :new-header-value (.. % -target -value))})
                  (map (fn [[header value]]
                         (dom/input #js {:className "input" :value header})
                         (dom/input #js {:className "input" :value value}))
                       (:headers form)))))

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
                                 (edit-headers form)
                                 (duration-selection form)
                                 (rate-selection form)
                                 (submit-form form api)
                                 (dom/div #js {:className "clearfix"})))))))
