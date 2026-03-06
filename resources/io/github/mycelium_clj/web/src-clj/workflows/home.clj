(ns {{top/ns}}.{{main/ns}}.workflows.home
  (:require [mycelium.core :as myc]
            ;; Load cell definitions
            [{{top/ns}}.{{main/ns}}.cells.home]))

(def workflow-def
  {:cells    {:start  :request/parse-home
              :render :page/render-home}
   :pipeline [:start :render]})

(def compiled (myc/pre-compile workflow-def))
