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

Visit `http://gocardless.dev:3010/`

### REPL

#### Without fireplace.vim:

Run `lein repl`, then:

```clj
(do (require 'cemerick.piggieback)
    (require 'cljs.repl)
    (require 'cljs.repl.browser)
    (cemerick.piggieback/cljs-repl (cljs.repl.browser/repl-env)
                                   :output-to  "resources/public/js/app.js"
                                   :output-dir "resources/public/js/out"))
```

You will now be able to run ClojureScript in this REPL.

#### With fireplace.vim

Run `lein repl`

In vim, open a clojurescript file, then run `:Piggieback 9000`. Refresh your browser to
make the connection.

## License

Copyright © 2015 GoCardless

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
