(ns load-test-om.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defonce app-state
  (atom {:text "GoCardless Enterprise API Load Tester"
         :url "https://staging.gocardless.com"
         :selected-resource ["Creditors" "Create"]
         :resources {"Creditors"              ["Create" "Index" "Show" "Update"]
                     "Creditor Bank Accounts" ["Create" "Index" "Show" "Disable"]
                     "Customers"              ["Create" "Index" "Show" "Update"]
                     "Customer Bank Accounts" ["Create" "Index" "Show" "Disable"]
                     "Mandates"               ["Create" "Index" "Show" "Update" "Cancel" "Reinstate"]
                     "Payments"               ["Create" "Index" "Show" "Update" "Cancel"]
                     "Payouts"                ["Index"]
                     "Subscriptions"          ["Create" "Index" "Show" "Update" "Cancel"]}}))

(enable-console-print!)

(comment
  (identity @app-state))

(defn handle-input-change [app e]
  (om/transact! app [:selected-resource 0] #(.. e -target -value))
  (om/transact! app [:selected-resource 1]
                #(first (get (:resources @app)
                             (first (:selected-resource @app))))))

(defn handle-action-change [app e]
  (om/transact! app [:selected-resource 1] #(.. e -target -value)))

(defn endpoint-selection [app]
  (dom/div
    #js {:className "load-test-form--field load-test-form--field__endpoint"}
    (dom/div #js {:className "label"} "Endpoint:")
    (dom/input #js {:type "text"
                    :value (:url app)
                    :className "input"
                    :disabled "true"})))

(defn resource-selection [app]
  (dom/div #js {:className "load-test-form--field load-test-form--field__resource"}
           (dom/div #js {:className "label"} "Resource:")
           (apply dom/select #js {:className "input"
                                  :value (first (:selected-resource app))
                                  :onChange (partial handle-input-change app)}
                  (map #(dom/option #js {:value %} %)
                       (keys (:resources app))))))

(defn action-selection [app]
  (dom/div #js {:className "load-test-form--field load-test-form--field__action"}
           (dom/div #js {:className "label"} "Action:")
           (apply dom/select #js {:className "input"
                                  :value (second (:selected-resource app))
                                  :onChange (partial handle-action-change app)}
                  (map #(dom/option #js {:value %} %)
                       (get (:resources app) (first (:selected-resource app)))))))

(defn submit-form [app]
  (dom/div #js {:className "load-test-form--field load-test-form--field__button"}
           (dom/div #js {:className "label"} "\u00A0")
           (dom/div #js {:className "btn btn-block"
                         :onClick (partial println "handlesubmit-form")}
                    "Start")))

(defn load-test-form [app]
  (dom/div #js {:className "container"}
           (dom/div #js {:className "main"}
                    (dom/div #js {:className "well load-test-form"}
                             (endpoint-selection app)
                             (resource-selection app)
                             (action-selection app)
                             (submit-form app)
                             (dom/div #js {:className "clearfix"})))))

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IRender
        (render [_]
          (dom/div nil
                   (dom/header nil
                               (dom/div #js {:className "container"}
                                        (dom/h2 #js {:id "title"} (:text app))))
                   (dom/div #js {:className "container"}
                            (dom/div #js {:className "main"}
                                     (load-test-form app)
                                     (dom/div #js {:className "hr"})
                                     "LoadTests"))))))
    app-state
    {:target (. js/document (getElementById "app"))}))
