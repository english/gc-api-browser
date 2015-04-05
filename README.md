# load-test-om

Front end for https://github.com/english/load-test-server. Allows users to trigger load
tests for a chosen resource/action and view the results as a histogram.

## User Workflow
1. Choose json schema (default to GC schema url)
2. Click "Load"
3. Select Resource (creditors, customers etc.)
4. Select Action (index, show etc.)
5. Select Duration
6. Select Rate
7. Click "Go"

## Development

1. Run `foreman start -f Procfile.dev`.
2. Hit `http://localhost:8000` and check the browser repl has connected (by not having a
  console error)
3. In Vim, with fireplace.vim installed, open a `.cljs` file and run `:Piggieback 9000`

## License

Copyright © 2014 FIXME

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
