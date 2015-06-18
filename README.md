# GC API Browser

## Install

Dependencies:
- [Leiningen](http://leiningen.org/)

```
brew install leiningen
lein deps
```

## Local development

### Run Figwheel

`lein figwheel`

This will compile ClojureScript and CSS and live update the browser when these change.
It will also serve these static files (and index.html) in development..

### REPL

Run `lein repl`, then:

```clj
(do (require 'cemerick.piggieback)
    (require 'cljs.repl)
    (require 'cljs.repl.browser)
    (cemerick.piggieback/cljs-repl (cljs.repl.browser/repl-env)
                                   :output-to  "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"
                                   :main       "gc-api-browser.main"
                                   :asset-path "/js/out"
                                   :verbose    true
                                   :optimizations :none))
```

You will now be able to run ClojureScript in this REPL.

## License

Copyright © 2015 GoCardless

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
