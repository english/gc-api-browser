(ns gc-api-browser.schema-select-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [gc-api-browser.schema-select :refer [get-domain]]))

(def schema
  {:links [{:href "http://api.gocardless.com"}
           {:href "http://sandbox-api.gocardless.com"}]})

(deftest test-get-domain
  (testing "with an existing url set"
    (is (= "http://example.com"
           (get-domain schema {:url "http://example.com/foobar"}))))
  (testing "with no existing url set"
    (is (= "http://sandbox-api.gocardless.com"
           (get-domain schema {:url nil})))))

(comment
  (run-tests)
  )
