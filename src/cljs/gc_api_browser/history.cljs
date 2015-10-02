(ns gc-api-browser.history
  (:require [om.core :as om :include-macros true]
            [om.dom :as dom :include-macros true]
            [clojure.set :as set]
            [gc-api-browser.utils :refer [log]]))

(defn at-start? [history id]
  (= id (:id (first history))))

(defn can-go-back? [{:keys [history history-id]}]
  (if history-id
    (not (at-start? history history-id))
    (seq history)))

(defn go-back [app]
  {:pre [(can-go-back? app)]}
  (let [entry (if-let [id (:history-id app)]
                (last (take-while #(not= id (:id %)) (:history app)))
                (last (:history app)))]
    (merge app (set/rename-keys entry {:id :history-id}))))

(defn can-go-forward? [{:keys [history-id history]}]
  (and history-id (not= history-id (:id (last history)))))

(defn go-forward [app]
  {:pre [(can-go-forward? app)]}
  (let [entry (second (drop-while #(not= (:history-id app) (:id %)) (:history app)))]
    (merge app (set/rename-keys entry {:id :history-id}))))

(defn render-paginator [app]
  (dom/div #js {:className "url-bar__item url-bar__item--paginator paginator"}
           (dom/button #js {:className "paginator__item btn"
                            :onClick #(om/transact! app go-back)
                            :disabled (not (can-go-back? app))} "<")
           (dom/button #js {:className "paginator__item btn"
                            :onClick #(om/transact! app go-forward)
                            :disabled (not (can-go-forward? app))} ">")))
