(ns gc-api-browser.json-pointer-test
  (:refer-clojure :exclude [get-in])
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [gc-api-browser.json-pointer :refer [get-in]]))

(deftest test-get-in
  (testing "one level of referencing"
    (is (= "baz"
           (get-in {:foo {:$ref "/bar"}
                    :bar "baz"}
                   [:foo]))))

  (testing "two levels of referencing"
    (is (= {:e :f}
           (get-in {:a {:$ref "/b"}
                    :b {:$ref "/c/d"}
                    :c {:d {:e :f}}}
                   [:a]))))

  (testing "nested get via two levels of referencing"
    (is (= :f
           (get-in {:a {:$ref "/b"}
                    :b {:$ref "/c/d"}
                    :c {:d {:e :f}}}
                   [:a :e])))))

(comment
  (run-tests)
  )
