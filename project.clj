(defproject load-test-om "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}

  :source-paths ["src/clj" "src/cljs"]
  :clean-targets ^{:protect false} ["resources/public/js/app.js" "resources/public/js/out"]

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/clojurescript "0.0-3058"]
                 [org.clojure/core.async "0.1.346.0-17112a-alpha"]
                 [ring "1.3.2"]
                 [ring/ring-defaults "0.1.3"]
                 [compojure "1.3.2"]
                 [enlive "1.1.5"]
                 [org.omcljs/om "0.8.8"]
                 [com.cemerick/piggieback "0.1.6-SNAPSHOT"]
                 [environ "1.0.0"]]

  :plugins [[lein-environ "1.0.0"]]

  :min-lein-version "2.5.0"

  :uberjar-name "load-test-om.jar"

  :profiles {:dev {:source-paths ["env/dev/clj"]
                   :test-paths ["test/clj"]
                   :dependencies [[org.clojure/tools.nrepl "0.2.7"]]

                   :repl-options {:init-ns load-test-om.server
                                  :nrepl-middleware [cemerick.piggieback/wrap-cljs-repl]}

                   :env {:is-dev true}}

             :uberjar {:source-paths ["env/prod/clj"]
                       :env {:production true}
                       :omit-source true
                       :aot :all
                       :cljsbuild {:builds {:app
                                            {:source-paths ["env/prod/cljs"]
                                             :compiler
                                             {:optimizations :advanced
                                              :pretty-print false}}}}}})
