(ns gc-api-browser.core-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [om.core :as om]
            [gc-api-browser.core :as core]))

(deftest test-handle-response
  (let [old-state {:request {:foo "bar"}
                   :response nil
                   :history []}
        request {:req "uest"}
        response {:resp "onse"}
        new-state (core/handle-response request response old-state)]

    (is (instance? UUID (:history-id new-state)))
    (is (= {:foo "bar"} (:request new-state)))
    (is (= {:resp "onse"} (:response new-state)))
    (is (= 1 (count (:history new-state))))

    (let [{:keys [request response id]} (first (:history new-state))]
      (is (= {:req "uest"} request))
      (is (= {:resp "onse"} response))
      (is (instance? UUID id)))))

(comment
  (run-tests)
  )
