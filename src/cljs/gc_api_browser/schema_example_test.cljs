(ns gc-api-browser.schema-example-test
  (:require [cljs.test :refer-macros [deftest is testing run-tests]]
            [gc-api-browser.schema-example :refer [prettify]]))

(deftest example-parsing
  (testing "response only"
    (let [example-str "POST https://api.gocardless.com/access_tokens/AC123/actions/disable HTTP/1.1

HTTP/1.1 200 (OK)
{
  \"access_tokens\": {
    \"id\": \"AC123\",
    \"created_at\": \"2014-05-08T17:01:06.000Z\",
    \"enabled\": false,
    \"token\": \"123ABC456DEF\",
    \"name\": \"Example access token\",
    \"scope\": \"full_access\"
  }
}
"
          expected-output ""]
      (is (= expected-output (prettify example-str)))))

  (testing "request and response"
    (let [example-str "POST https://api.gocardless.com/bank_details_lookups HTTP/1.1
{
  \"bank_details_lookups\": {
    \"account_number\": \"55779911\",
    \"branch_code\": \"200000\",
    \"country_code\": \"GB\"
  }
}

HTTP/1.1 200 (OK)
{
  \"bank_details_lookups\": {
    \"available_debit_schemes\": [\"bacs\"],
    \"bank_name\": \"BARCLAYS BANK PLC\",
    \"bic\": \"BARCGB22XXX\"
  }
}

"
          expected-output "{
  \"bank_details_lookups\": {
    \"account_number\": \"55779911\",
    \"branch_code\": \"200000\",
    \"country_code\": \"GB\"
  }
}"]
      (is (= expected-output (prettify example-str)))))

  (testing "a PUT"
      (let [example-str "PUT https://api.gocardless.com/billing_contacts/BC123 HTTP/1.1
{
  \"billing_contacts\": {
    \"given_name\": \"Newname\"
  }
}

HTTP/1.1 200 (OK)
{
  \"billing_contacts\": {
    \"id\": \"BC123\",
    \"created_at\": \"2014-05-27T12:43:17.000Z\",
    \"email\": \"someone@example.com\",
    \"phone_number\": \"+44 20 7123 4567\",
    \"given_name\": \"Newname\",
    \"family_name\": \"Osborne\"
  }
}
"
            expected-output "{
  \"billing_contacts\": {
    \"given_name\": \"Newname\"
  }
}"]
        (is (= expected-output (prettify example-str))))))

(comment
  (run-tests))
