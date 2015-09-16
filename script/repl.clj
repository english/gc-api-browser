(require '[figwheel-sidecar.repl])
(require '[figwheel-sidecar.repl-api])

(figwheel-sidecar.repl-api/start-figwheel!
 {:figwheel-options {:http-server-root "public"
                     :server-port      3010
                     :css-dirs         ["resources/public/css"]}
  :all-builds       (figwheel-sidecar.repl/get-project-cljs-builds)})

(figwheel-sidecar.repl-api/start-autobuild "main")

(comment (figwheel-sidecar.repl-api/cljs-repl))
