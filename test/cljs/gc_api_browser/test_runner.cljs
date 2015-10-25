(ns gc-api-browser.test-runner
  (:require [cljs.test :refer-macros [run-tests]]
            [gc-api-browser.history-test]
            [gc-api-browser.json-pointer-test]
            [gc-api-browser.json-schema-test]
            [gc-api-browser.schema-example-test]
            [gc-api-browser.schema-select-test]
            [gc-api-browser.url-bar-test]))

(run-tests
  'gc-api-browser.history-test
  'gc-api-browser.json-pointer-test
  'gc-api-browser.json-schema-test
  'gc-api-browser.schema-example-test
  'gc-api-browser.schema-select-test
  'gc-api-browser.url-bar-test)
