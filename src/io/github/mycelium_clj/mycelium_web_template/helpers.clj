(ns io.github.mycelium-clj.mycelium-web-template.helpers
  (:require [selmer.parser :as selmer]))

(defn render-selmer
  [text options]
  (selmer/render
   (str "<% safe %>" text "<% endsafe %>")
   options
   {:tag-open \< :tag-close \> :filter-open \< :filter-close \>}))
