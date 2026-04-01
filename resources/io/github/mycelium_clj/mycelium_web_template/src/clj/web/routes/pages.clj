(ns <<ns-name>>.web.routes.pages
  (:require [integrant.core :as ig]
            [mycelium.middleware :as mw]
            [<<ns-name>>.workflows.home :as home]))

(defn page-routes [{:keys [db]}]
  [["/" {:get {:handler (mw/workflow-handler home/compiled
                          {:resources {:db db}})}}]])

(derive :reitit.routes/pages :reitit/routes)

(defmethod ig/init-key :reitit.routes/pages
  [_ opts]
  (fn []
    ["" (page-routes opts)]))
