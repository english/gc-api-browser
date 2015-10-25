(ns gc-api-browser.url-bar-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [gc-api-browser.url-bar :as url-bar]))

(deftest test-handle-response
  (let [old-state {:request {:foo "bar"}
                   :response nil
                   :history []}
        response {:resp "onse"}
        new-state (url-bar/handle-response response old-state old-state)]
    (is (instance? UUID (:history-id new-state)))
    (is (= {:foo "bar"} (:request new-state)))
    (is (= {:resp "onse"} (:response new-state)))
    (is (= 1 (count (:history new-state))))

    (let [{:keys [request response id]} (first (:history new-state))]
      (is (= {:foo "bar"} request))
      (is (= {:resp "onse"} response))
      (is (instance? UUID id)))))

(comment
  (run-tests)
  )
