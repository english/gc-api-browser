(ns load-test-client.form
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [goog.json :as gjson]
            [goog.events :as events]
            [cljs.core.async :refer [put! chan <!]])
  (:import [goog.net XhrIo EventType]
           [goog json]))

(defn request-for [schema resource action]
  (let [prefix "https://api-staging.gocardless.com"
        link (first (filter #(= (:rel %) action)
                            (get-in schema [:definitions (keyword resource) :links])))]
    {:method (:method link)
     :url (str prefix (:href link))}))

(defn actions-for-resource [schema resource]
  (map :rel (get-in schema [:definitions (keyword resource) :links])))

(defn read-as-text [file c]
  (let [reader (js/FileReader.)]
    (set! (.-onload reader) #(put! c (.. % -target -result)))
    (.readAsText reader file)
    c))

(defn schema->resources [schema]
  (map name (keys (:definitions schema))))

(defn set-selected-action! [form schema resource action]
  (om/update! form :selected-action action)
  (om/transact! form (fn [m]
                       (merge m (request-for schema resource action)))))

(defn set-selected-resource! [form schema resource]
  (om/update! form :selected-resource resource)
  (set-selected-action! form schema resource (first (actions-for-resource schema resource))))

(defn set-schema! [form schema]
  (om/update! form :schema schema)
  (set-selected-resource! form schema (first (schema->resources schema))))

(defn handle-schema-input-change [form evt]
  (let [file (first (array-seq (.. evt -target -files)))]
    (go (let [text (<! (read-as-text file (chan)))]
          (set-schema! form (-> text gjson/parse (js->clj :keywordize-keys true)))))))

(defn handle-resource-change [form e]
  (set-selected-resource! form (:schema form) (.. e -target -value)))

(defn handle-action-change [form e]
  (set-selected-action! form (:schema form) (:selected-resource form) (.. e -target -value)))

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
                                 (dom/div #js {:className "clearfix"})
                                 (edit-url form)
                                 (duration-selection form)
                                 (rate-selection form)
                                 (submit-form form api)
                                 (dom/div #js {:className "clearfix"})))))))
