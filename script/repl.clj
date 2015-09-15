(use 'figwheel-sidecar.repl-api)

(start-figwheel!
 {:figwheel-options {:http-server-root "public"
                     :server-port      3010
                     :css-dirs         ["resources/public/css"]}
  :all-builds       (figwheel-sidecar.repl/get-project-cljs-builds)})

(cljs-repl)