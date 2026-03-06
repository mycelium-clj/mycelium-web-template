(ns {{top/ns}}.{{main/ns}}.env
  (:require [clojure.tools.logging :as log]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[{{raw-name}} starting]=-"))
   :start      (fn []
                 (log/info "\n-=[{{raw-name}} started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[{{raw-name}} has shut down successfully]=-"))
   :middleware (fn [handler _] handler)
   :opts       {:profile :prod}})
