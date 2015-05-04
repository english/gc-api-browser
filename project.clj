(defproject gc-api-browser "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]
  :clean-targets ^{:protect false} ["resources/public/js/app.js" "resources/public/js/out"]

  :dependencies [[org.clojure/clojure "1.7.0-beta2"]
                 [org.clojure/clojurescript "0.0-3211"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.4"]
                 [compojure "1.3.3"]
                 [org.omcljs/om "0.8.8"]
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

  :profiles {:dev {:dependencies [[com.cemerick/piggieback "0.2.1"]
                                  [org.clojure/tools.nrepl "0.2.10"]]

                   :repl-options {:nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}}

             :uberjar {:omit-source true
                       :aot :all
                       :cljsbuild {:builds {:main
                                            {:compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
