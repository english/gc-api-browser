(ns gc-api-browser.history-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [om.core :as om]
            [gc-api-browser.history :as history]))

(deftest test-go-back
  (let [history [{:request "first request" :response "first response" :id "1"}
                 {:request "second request" :response "second response" :id "2"}]
        app-state {:request "current request"
                   :response "current response"
                   :history history
                   :history-id "2"}]
    (is (= {:request "first request"
            :response "first response"
            :history-id "1"
            :history history}
           (history/go-back app-state)))))

(deftest test-go-forward
  (let [history [{:request "first request" :response "first response" :id "1"}
                 {:request "second request" :response "second response" :id "2"}]
        app-state {:request "current request"
                   :response "current response"
                   :history history
                   :history-id "1"}]
    (is (= {:request "second request"
            :response "second response"
            :history-id "2"
            :history history}
           (history/go-forward app-state)))))

(comment
  (run-tests)
  )
