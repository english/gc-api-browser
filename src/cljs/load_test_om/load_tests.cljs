(ns load-test-om.load-tests
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]))

(defn load-tests [load-tests owner]
  (reify
    om/IWillMount
    (will-mount [_]
      (let [fb-ref (js/Firebase. "https://flickering-heat-5516.firebaseio.com/loadTests")]
        (.on fb-ref "child_added"
             (fn [snapshot]
               (let [v (.val snapshot)
                     load-test {:id (.key snapshot)
                                :resource (.-resource v)
                                :action (.-action v)}]
                 (om/transact! load-tests :items #(conj % load-test)))))))
    om/IRender
    (render [_]
      (apply dom/ul nil (map #(dom/li nil (:id %)) (:items load-tests))))))
