(ns load-test-client.main
  (:require [load-test-client.core :as core]
            [clojure.browser.repl :as repl]))

(defonce conn
  (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(core/main)
