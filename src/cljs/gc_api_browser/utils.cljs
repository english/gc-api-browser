(ns gc-api-browser.utils
  (:require [cljs.pprint :as pprint]))

(defn log [object]
  (js/console.log (with-out-str (pprint/pprint object)))
  object)
