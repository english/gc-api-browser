(ns load-test-om.build
  (:require [cljs.closure :as closure])
  (:gen-class))

(defn -main [& args]
  (let [build-opts {:output-to "resources/public/js/app.js"
                    :output-dir "resources/public/js/out"
                    :main 'load-test-om.main
                    :asset-path "/js/out"
                    :verbose true}]
  (closure/build "src" build-opts)
  (closure/watch "src" build-opts)))
