(ns load-test-om.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [load-test-om.form :as form]
            [load-test-om.load-tests :as load-tests]))

(defonce app-state
  (atom {:text "GoCardless Enterprise API Load Tester"
         :form {:url "https://staging.gocardless.com"
                :selected-resource ["Creditors" "Create"]
                :resources {"Creditors"              ["Create" "Index" "Show" "Update"]
                            "Creditor Bank Accounts" ["Create" "Index" "Show" "Disable"]
                            "Customers"              ["Create" "Index" "Show" "Update"]
                            "Customer Bank Accounts" ["Create" "Index" "Show" "Disable"]
                            "Mandates"               ["Create" "Index" "Show" "Update" "Cancel" "Reinstate"]
                            "Payments"               ["Create" "Index" "Show" "Update" "Cancel"]
                            "Payouts"                ["Index"]
                            "Subscriptions"          ["Create" "Index" "Show" "Update" "Cancel"]}}
         :load-tests {:url ""
                      :items []}}))

(enable-console-print!)

(comment
  (identity @app-state))

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
                                     (om/build form/load-test-form (:form app))
                                     (dom/div #js {:className "hr"})
                                     (om/build load-tests/load-tests (:load-tests app))))))))
    app-state
    {:target (. js/document (getElementById "app"))}))
