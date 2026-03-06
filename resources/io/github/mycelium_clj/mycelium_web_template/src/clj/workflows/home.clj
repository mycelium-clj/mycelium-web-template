(ns <<ns-name>>.workflows.home
  (:require [mycelium.core :as myc]
            ;; Load cell definitions
            [<<ns-name>>.cells.home]))

(def workflow-def
  {:cells    {:start  :request/parse-home
              :render :page/render-home}
   :pipeline [:start :render]})

(def compiled (myc/pre-compile workflow-def))
