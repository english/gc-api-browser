(ns gc-api-browser.dom-utils
  (:require [om.dom :as dom :include-macros true]))

(def clearfix (dom/div #js {:className "clearfix"}))
