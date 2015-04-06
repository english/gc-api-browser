(ns load-test-client.form
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.json :as gjson]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]])
  (:import [goog.net XhrIo EventType]
           [goog json]))

(defn actions-for-resource [schema resource]
  (map :rel (get-in schema [:definitions (keyword resource) :links])))

(defn handle-resource-change [form e]
  (let [resource (.. e -target -value)]
    (om/update! form :selected-resource resource)
    (om/update! form :selected-action (first (actions-for-resource (:schema form) resource)))))

(defn handle-action-change [form e]
  (om/update! form :selected-action (.. e -target -value)))

(defn read-as-text [file c]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader) #(put! c (.. % -target -result)))
    (.readAsText reader file)
    c))

(defn schema->resources [schema]
  (map name (keys (:definitions schema))))

(defn handle-schema-input-change [form evt]
  (let [file (first (array-seq (.. evt -target -files)))]
    (go (let [text (<! (read-as-text file (chan)))
              schema (-> text gjson/parse (js->clj :keywordize-keys true))
              resource (first (schema->resources schema))]
          (om/update! form :schema schema)
          (om/update! form :selected-resource resource)
          (om/update! form :selected-action (first (actions-for-resource schema resource)))))
    false))

(defn schema-file [form]
  (dom/div
    #js {:className "load-test-form--field load-test-form--field__schema"}
    (dom/div #js {:className "label"} "Schema:")
    (dom/input #js {:type "file"
                    :className "input"
                    :accept "application/json"
                    :onChange (partial handle-schema-input-change form)})))

(defn resource-selection [{:keys [selected-resource selected-action schema] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__resource"}
           (dom/div #js {:className "label"} "Resource:")
           (apply dom/select #js {:className "input"
                                  :value selected-resource
                                  :onChange (partial handle-resource-change form)}
                  (map #(dom/option #js {:value %} %)
                       (schema->resources schema)))))

(defn action-selection [{:keys [selected-resource selected-action schema] :as form}]
  (dom/div #js {:className "load-test-form--field load-test-form--field__action"}
           (dom/div #js {:className "label"} "Action:")
           (apply dom/select #js {:className "input"
                                  :value (when selected-action (name selected-action))
                                  :onChange (partial handle-action-change form)}
                  (map #(dom/option #js {:value %} %)
                       (actions-for-resource schema selected-resource)))))

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

(defn handle-submit [{:keys [selected-resource selected-action duration rate] :as form} {:keys [http-url] :as api}]
  (let [duration (js/parseInt duration)
        rate (js/parseInt rate)]
    (doto (XhrIo.)
      (events/listen EventType.SUCCESS #(.log js/console "SUCCESS" %))
      (events/listen EventType.ERROR #(.log js/console "ERROR" %))
      (.send (str http-url "load-tests")
             "POST"
             (.serialize json (clj->js {:resource selected-resource :action selected-action :duration duration :rate rate}))
             #js {"Content-Type" "application/json"}))))

(defn submit-form [form api]
  (dom/div #js {:className "load-test-form--field load-test-form--field__button"}
           (dom/div #js {:className "label"} "\u00A0")
           (dom/div #js {:className "btn btn-block"
                         :onClick (partial handle-submit form api)}
                    "Start")))

(defn component [{:keys [form api]} owner]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "container"}
               (dom/div #js {:className "main"}
                        (dom/div #js {:className "well load-test-form"}
                                 (schema-file form)
                                 (resource-selection form)
                                 (action-selection form)
                                 (duration-selection form)
                                 (rate-selection form)
                                 (submit-form form api)
                                 (dom/div #js {:className "clearfix"})))))))
