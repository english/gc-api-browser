(ns load-test-om.main
  (:require [load-test-om.core :as core]
            [clojure.browser.repl :as repl]))

(defonce conn
  (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(core/main)
