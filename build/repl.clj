(ns load-test-om.repl
  (:require [cljs.closure :as closure]
            [cljs.repl :as repl]
            [cljs.repl.browser :as browser-repl]))

(def build-opts {:output-to "resources/public/js/app.js"
                 :output-dir "resources/public/js/out"
                 :main 'load-test-om.main
                 :asset-path "/js/out"
                 :verbose true})

(closure/build "src" build-opts)
(closure/watch "src" build-opts)
