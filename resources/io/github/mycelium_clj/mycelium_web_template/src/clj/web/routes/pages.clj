(ns <<ns-name>>.web.routes.pages
  (:require [integrant.core :as ig]
            [mycelium.middleware :as mw]
            [<<ns-name>>.workflows.home :as home]))

(defn page-routes [opts]
  ;; All integrant-injected resources are passed through to mycelium cells.
  ;; Cells access them via (fn [resources data] ...) where resources = opts.
  [["/" {:get {:handler (mw/workflow-handler home/compiled
                          {:resources opts})}}]])

(derive :reitit.routes/pages :reitit/routes)

(defmethod ig/init-key :reitit.routes/pages
  [_ opts]
  (fn []
    ["" (page-routes opts)]))
