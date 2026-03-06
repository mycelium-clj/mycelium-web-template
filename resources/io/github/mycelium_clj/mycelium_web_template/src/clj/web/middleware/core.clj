(ns <<ns-name>>.web.middleware.core
  (:require
    [<<ns-name>>.env :as env]
    [ring.middleware.defaults :as defaults]))

(defn wrap-base
  [{:keys [site-defaults-config] :as opts}]
  (fn [handler]
    (cond-> ((:middleware env/defaults) handler opts)
      true (defaults/wrap-defaults site-defaults-config))))
