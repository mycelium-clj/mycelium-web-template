(ns <<ns-name>>.cells.home
  (:require [mycelium.core :as myc]
            [selmer.parser :as selmer]))

(myc/defcell :request/parse-home
  {:input  {:http-request :map}
   :output {:name :string}
   :doc    "Extract name parameter from the HTTP request"}
  (fn [_resources data]
    (let [params (or (get-in data [:http-request :query-params]) {})
          name   (or (get params "name") (get params :name) "World")]
      {:name name})))

(myc/defcell :page/render-home
  {:input  {:name :string}
   :output {:html :string}
   :doc    "Render the home page HTML"}
  (fn [_resources data]
    {:html (selmer/render-file "html/home.html"
                               {:name (:name data)})}))
