(ns {{top/ns}}.{{main/ns}}.env
  (:require
    [clojure.tools.logging :as log]
    [{{top/ns}}.{{main/ns}}.dev-middleware :refer [wrap-dev]]))

(def defaults
  {:init       (fn []
                 (log/info "\n-=[{{raw-name}} starting using the development profile]=-"))
   :start      (fn []
                 (log/info "\n-=[{{raw-name}} started successfully]=-"))
   :stop       (fn []
                 (log/info "\n-=[{{raw-name}} has shut down successfully]=-"))
   :middleware wrap-dev
   :opts       {:profile :dev}})
