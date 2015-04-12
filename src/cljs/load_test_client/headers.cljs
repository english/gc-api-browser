(ns load-test-client.headers
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.string :as string])
  (:import [goog.ui IdGenerator]))

(defn header-component [[header-name header-value] owner {:keys [handle-delete] :as opts}]
  (reify
    om/IRender
    (render [_]
      (dom/div #js {:className "headers__header"}
               (dom/div #js {:className "headers__header__name"}
                        (dom/input #js {:className "input"
                                        :disabled true
                                        :value header-name}))
               (dom/span #js {:className "headers__header__separator"} ":")
               (dom/div #js {:className "headers__header__value"}
                        (dom/input #js {:className "input"
                                        :disabled true
                                        :value header-value}))
               (dom/button #js {:type "button"
                                :className "headers__header__delete"
                                :onClick handle-delete} "✖")))))

(defn delete-header [headers k]
  (om/transact! headers #(dissoc % k)))

(def ENTER_KEY 13)

(defn handle-new-header-keydown [e headers owner]
  (when (== (.-which e) ENTER_KEY)
    (let [name-node (om/get-node owner "newHeaderNameField")
          value-node (om/get-node owner "newHeaderValueField")]
      (when-not (or (string/blank? (.. name-node -value trim))
                    (string/blank? (.. value-node -value trim)))
        (om/transact! headers #(assoc % (.-value name-node) (.-value value-node)))
        (set! (.-value name-node) "")
        (set! (.-value value-node) "")
        (.focus name-node)))
    (.preventDefault e)))

(defn component [headers owner]
  (reify
    om/IRender
    (render [_]
      (apply dom/div #js {:className "load-test-form--field headers"}
             (dom/div #js {:className "label"} "Headers")
             (dom/div #js {:className "headers__header"}
                      (dom/div #js {:className "headers__header__name"}
                               (dom/input #js {:className "input"
                                               :ref "newHeaderNameField"
                                               :placeholder "Header Name"
                                               :onKeyDown #(handle-new-header-keydown % headers owner)}))
                      (dom/span #js {:className "headers__header__separator"} ":")
                      (dom/div #js {:className "headers__header__value"}
                               (dom/input #js {:className "input"
                                               :ref "newHeaderValueField"
                                               :placeholder "Header Value"
                                               :onKeyDown #(handle-new-header-keydown % headers owner)})))
             (map (fn [[k v]]
                    (om/build header-component [k v] {:opts {:handle-delete #(delete-header headers k)}}))
                  headers)))))
