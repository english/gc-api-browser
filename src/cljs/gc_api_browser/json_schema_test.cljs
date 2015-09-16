(ns gc-api-browser.json-schema-test
  (:require [gc-api-browser.json-schema-fixture :refer [fixture]]
            [gc-api-browser.json-schema :as json-schema]
            [cljs.test :refer-macros [deftest is testing run-tests]]))

(deftest test-json-schema
  (testing "schema->resources"
    (is (= '("Creditor Bank Accounts" "Creditors")
           (json-schema/schema->resources fixture))))

  (testing "resource->actions"
    (is (= '("Create a creditor bank account"
             "Disable a creditor bank account"
             "Get a single creditor bank account"
             "List creditor bank accounts")
           (json-schema/resource->actions fixture "Creditor Bank Accounts")))
    (is (= '()
           (json-schema/resource->actions fixture "Doesn't exist"))))

  (testing "process-href"
    (is (= "/creditors/CR123"
           (json-schema/process-href "/creditors/{(%23%2Fdefinitions%2Fcreditor%2Fdefinitions%2Fidentity)}" fixture)))
    (is (= "/creditors"
           (json-schema/process-href "/creditors" fixture))))

  (testing "schema->domain"
    (is (= "https://api.gocardless.com"
           (json-schema/schema->domain fixture))))

  (testing "schema->action-node"
    (is (= {:method  "POST"
            :href    "/creditor_bank_accounts"
            :example "{\n  \"creditor_bank_accounts\": {\n    \"account_number\": \"55779911\",\n    \"branch_code\": \"200000\",\n    \"country_code\": \"GB\",\n    \"set_as_default_payout_account\": true,\n    \"account_holder_name\": \"Nude Wines\",\n    \"links\": {\n      \"creditor\": \"CR123\"\n    }\n  }\n}"}
           (json-schema/schema->action-node fixture "Creditor Bank Accounts" "Create a creditor bank account")))))

(comment
 (run-tests)
 )