(ns <<ns-name>>.db
  "SQLite database integrant component.
   Provides a JDBC datasource that flows into mycelium cell handlers via resources."
  (:require [integrant.core :as ig]
            [next.jdbc :as jdbc]))

(defmethod ig/init-key :db/sqlite [_ {:keys [dbname]}]
  (let [ds (jdbc/get-datasource {:dbtype "sqlite" :dbname dbname})]
    ds))

(defmethod ig/halt-key! :db/sqlite [_ _ds]
  nil)
