(ns load-test-om.load-tests
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.set :refer [rename]]
            [load-test-om.load-test :as load-test]))

(def firebase-url "https://flickering-heat-5516.firebaseio.com/loadTests")

(defn handle-removed-load-test [load-tests snapshot]
  (let [snapshot-id (.key snapshot)]
    (om/transact! load-tests :items (fn [items]
                                      (remove #(= (:id %) snapshot-id) items)))))

(defn handle-new-load-test [load-tests snapshot]
  (let [v (.val snapshot)
        data-points (-> (.-dataPoints v)
                        (js->clj :keywordize-keys true)
                        vals
                        (->> (map #(select-keys % [:status :time :responseTime])))
                        (rename {:responseTime :response-time})
                        vec)
        load-test {:id (.key snapshot)
                   :resource (.-resource v)
                   :action (.-action v)
                   :data-points data-points}]
    (om/transact! load-tests :items (partial into [load-test]))))

(defn load-tests [load-tests owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [fb-ref (js/Firebase. firebase-url)]
        (om/set-state! owner :firebase-ref fb-ref)
        (-> fb-ref
            (.limitToLast 20)
            (.on "child_added" (partial handle-new-load-test load-tests)))
        (.on fb-ref "child_removed" (partial handle-removed-load-test load-tests))))

    om/IWillUnmount
    (will-unmount [_]
      (.off (om/get-state owner :firebase-ref))
      (om/update! load-tests :items []))

    om/IRender
    (render [_]
      (->> (:items load-tests)
           (sort-by :id)
           reverse
           (om/build-all load-test/load-test)
           (apply dom/ul nil)))))
