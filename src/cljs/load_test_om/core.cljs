(ns load-test-om.core
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [cljs.repl :as repl :include-macros true]
            [load-test-om.form :as form]
            [load-test-om.load-tests :as load-tests])
  (:import [goog.net XhrIo]))

(defonce app-state
  (atom {:text "GoCardless Enterprise API Load Tester"
         :form {}
         :load-tests {:items []}}))

(enable-console-print!)

(comment
  (get-in @app-state [:form :selected-resource])
  ;; how many load-tests
  (count (get-in @app-state [:load-tests :items]))
  ;; how many data points in first load test
  (count (get-in @app-state [:load-tests :items 0 :data-points]))
  ;; look at a data-point
  (get-in @app-state [:load-tests :items 0 :data-points 0])
  ;; look at the first load-test's stats
  (get-in @app-state [:load-tests :items 0 :stats])
  ;; Correct order?
  (apply > (map
             (fn [load-test] (get-in load-test [:data-points 0 :time]))
             (get-in @app-state [:load-tests :items])))
  ;; which has the most data-points
  (->> (get-in @app-state [:load-tests :items])
       (sort-by (comp count :data-points) >)
       first
       :id)
  ;; show first load test stats
  (->> (get-in @app-state [:load-tests :items 0 :stats])))

(comment
  (identity @app-state))

(defn initial-selected-resource [resources]
  [(first (first resources))
   (first (second (first resources)))])

(defn handle-preset-response [app e]
  (let [json (js->clj (.. e -target getResponseJson))]
    (om/transact! app :form (fn [form]
                              (assoc form
                                     :resources (get json "resources")
                                     :url (get json "url")
                                     :selected-resource (initial-selected-resource (get json "resources")))))))

(defn main []
  (om/root
    (fn [app owner]
      (reify
        om/IDidMount
        (did-mount [_]
          (.send XhrIo "http://localhost:3000/presets" (partial handle-preset-response app)))
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
