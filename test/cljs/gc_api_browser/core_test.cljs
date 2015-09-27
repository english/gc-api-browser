(ns gc-api-browser.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [om.core :as om]
            [gc-api-browser.core :as core]))

(deftest test-handle-response
  (let [old-state {:request {:foo "bar"}
                   :response nil
                   :history []}
        request {:req "uest"}
        response {:resp "once"}
        new-state (core/handle-response request response old-state)]

    (is (string? (:history-id new-state)))
    (is (= {:foo "bar"} (:request new-state)))
    (is (= {:resp "once"} (:response new-state)))
    (is (= 1 (count (:history new-state))))

    (let [{:keys [request response id]} (first (:history new-state))]
      (is (= {:req "uest"} request))
      (is (= {:resp "once"} response))
      (is (string? id)))))

(deftest test-go-back!
  (let [history [{:request "first request" :response "first response" :id "1"}
                 {:request "second request" :response "second response" :id "2"}]
        cursor (om/ref-cursor (om/root-cursor (atom {:request "current request"
                                                     :response "current response"
                                                     :history history
                                                     :history-id "2"})))]
    (core/go-back! cursor)

    (is (= {:request "first request"
            :response "first response"
            :history-id "1"
            :history history}
           @cursor))))

(comment
  (run-tests)
  )
