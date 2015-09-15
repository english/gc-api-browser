(ns gc-api-browser.utils
  (:require-macros [cljs.core.async.macros :refer [go]])
  (:require [cljs.pprint :as pprint]
            [cljs.core.async :as async]))

(defn log [object]
  (js/console.log (with-out-str (pprint/pprint object)))
  object)

; stolen from: https://gist.github.com/swannodette/5886048
(defn throttle [c ms]
  (let [c' (async/chan)]
    (go
     (while true
       (async/>! c' (async/<! c))
       (async/<! (async/timeout ms))))
    c'))
