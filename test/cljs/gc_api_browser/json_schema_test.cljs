(ns gc-api-browser.json-schema-test
  (:require [gc-api-browser.json-schema-fixture :refer [fixture]]
            [gc-api-browser.json-schema :as json-schema]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-schema->resources
  (is (= '("Creditor Bank Accounts" "Creditors")
         (json-schema/schema->resources fixture))))

(deftest test-resource->actions
  (testing "resource that exists"
    (is (= '("Create a creditor bank account"
             "Disable a creditor bank account"
             "Get a single creditor bank account"
             "List creditor bank accounts")
           (json-schema/resource->actions fixture "Creditor Bank Accounts"))))
  (testing "resource that doesn't exist"
    (is (= '()
           (json-schema/resource->actions fixture "Doesn't exist")))))

(deftest test-expand-href
  (testing "when input has a reference"
    (is (= "/creditors/CR123"
           (json-schema/expand-href "/creditors/{(%23%2Fdefinitions%2Fcreditor%2Fdefinitions%2Fidentity)}" fixture))))
  (testing "when input doesn't have a reference"
    (is (= "/creditors"
           (json-schema/expand-href "/creditors" fixture)))))

(deftest test-schema->domain
  (is (= "https://api-sandbox.gocardless.com"
         (json-schema/schema->domain fixture))))

(deftest schema->request
  (is (= {:method "POST"
          :path "/creditor_bank_accounts"
          :body "{\n  \"creditor_bank_accounts\": {\n    \"account_number\": \"55779911\",\n    \"branch_code\": \"200000\",\n    \"country_code\": \"GB\",\n    \"set_as_default_payout_account\": true,\n    \"account_holder_name\": \"Nude Wines\",\n    \"links\": {\n      \"creditor\": \"CR123\"\n    }\n  }\n}"}
         (json-schema/schema->request fixture "Creditor Bank Accounts" "Create a creditor bank account"))))

(comment
  (run-tests)
  )
