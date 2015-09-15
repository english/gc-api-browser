(ns gc-api-browser.json-pointer-test
  (:refer-clojure :exclude [get-in])
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [gc-api-browser.json-pointer :refer [get-in]]))

(deftest test-json-pointer
  (testing "get-in"
    (is (= "baz" (get-in {:foo {:$ref "/bar"}
                          :bar "baz"} [:foo])))

    (is (= {:e :f} (get-in {:a {:$ref "/b"}
                            :b {:$ref "/c/d"}
                            :c {:d {:e :f}}} [:a])))

    (is (= :f (get-in {:a {:$ref "/b"}
                       :b {:$ref "/c/d"}
                       :c {:d {:e :f}}} [:a :e])))))

(comment
 (run-tests))
