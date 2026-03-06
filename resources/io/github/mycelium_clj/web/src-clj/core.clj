(ns {{top/ns}}.{{main/ns}}.core
  (:require
    [clojure.tools.logging :as log]
    [integrant.core :as ig]
    [{{top/ns}}.{{main/ns}}.config :as config]
    [{{top/ns}}.{{main/ns}}.env :refer [defaults]]

    ;; Edges
    [kit.edge.server.jetty]
    [{{top/ns}}.{{main/ns}}.web.handler]

    ;; Routes
    [{{top/ns}}.{{main/ns}}.web.routes.pages])
  (:gen-class))

(Thread/setDefaultUncaughtExceptionHandler
  (fn [thread ex]
    (log/error {:what      :uncaught-exception
                :exception ex
                :where     (str "Uncaught exception on " (.getName thread))})))

(defonce system (atom nil))

(defn stop-app []
  ((or (:stop defaults) (fn [])))
  (some-> (deref system) (ig/halt!)))

(defn start-app [& [params]]
  ((or (:start params) (:start defaults) (fn [])))
  (->> (config/system-config (or (:opts params) (:opts defaults) {}))
       (ig/expand)
       (ig/init)
       (reset! system)))

(defn -main [& _]
  (start-app)
  (.addShutdownHook (Runtime/getRuntime)
                    (Thread. (fn [] (stop-app) (shutdown-agents)))))
