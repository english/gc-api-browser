(ns gc-api-browser.dev
  (:require [gc-api-browser.core :as core]
            [clojure.browser.repl :as repl]))

(defonce conn (repl/connect "http://localhost:9000/repl"))

(enable-console-print!)

(core/main)
