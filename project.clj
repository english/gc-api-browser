(defproject gc-api-browser "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]
  :clean-targets ^{:protect false} ["resources/public/js/app.js" "resources/public/js/out"]

  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/clojurescript "0.0-3126"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [org.omcljs/om "0.8.8"]
                 [cljs-http "0.1.30"]
                 [racehub/om-bootstrap "0.5.0"]]

  :plugins [[lein-cljsbuild "1.0.5"]
            [cider/cider-nrepl "0.8.2"]]

  :min-lein-version "2.5.0"

  :uberjar-name "gc-api-browser.jar"

  :cljsbuild {:builds [{:id :main
                        :source-paths ["src/cljs"]
                        :compiler {:output-to  "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"
                                   :main       "gc-api-browser.main"
                                   :asset-path "/js/out"
                                   :verbose    true
                                   :optimizations :none}}]}

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.1.6-SNAPSHOT"]
                                  [org.clojure/tools.nrepl "0.2.7"]]

                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

             :uberjar {:omit-source true
                       :aot :all
                       :cljsbuild {:builds {:main
                                            {:compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
