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

Run `lein repl`, then eval all of script/repl.clj.

This will compile ClojureScript and CSS and live update the browser when these change.
It will also serve these static files (and index.html) in development..

Visit `http://gocardless.dev:3010/`

### ClojureScript REPL

#### Without fireplace.vim:

```clj
(figwheel-sidecar.repl-api/cljs-repl)
```

You will now be able to run ClojureScript in this REPL.

#### With fireplace.vim

In vim, open a clojurescript file, then run `:Piggieback 9000`. Refresh your browser to
make the connection.

## License

Copyright © 2015 GoCardless

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
