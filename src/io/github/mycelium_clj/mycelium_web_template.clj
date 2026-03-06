(ns io.github.mycelium-clj.mycelium-web-template
  (:require
   [clojure.string :as str]
   [babashka.fs :as fs]
   [io.github.mycelium-clj.mycelium-web-template.helpers :as helpers]
   [selmer.parser :as selmer])
  (:import java.io.File))

(defn- ->ns
  [f]
  (-> f (str) (str/replace "/" ".") (str/replace "_" "-")))

(defn- ->file
  [n]
  (-> n (str) (str/replace "." "/") (str/replace "-" "_")))

(defn- selmer-opts
  [{:keys [name] :as data}]
  (let [parts (str/split name #"/")
        app   (last parts)]
    (merge data
           {:full-name name
            :app       (->file app)
            :ns-name   (->ns name)
            :sanitized (->file name)})))

(defn- adapt-separator [pattern]
  (let [separator File/separator
        escaped   (if (= "\\" separator) "\\\\" separator)]
    (str/replace pattern "/" escaped)))

(defn- match-namespaced-file
  [file-path]
  (let [pattern1 (adapt-separator #"^((?:src|test)/clj)/(.+)$")
        pattern2 (adapt-separator #"^(env/(?:dev|prod)/clj)/((?:dev_middleware|env)\.clj)$")]
    (or (let [[[_ prefix suffix]] (re-seq (re-pattern pattern1) file-path)]
          (when (and prefix suffix)
            {:prefix prefix :suffix suffix}))
        (let [[[_ prefix suffix]] (re-seq (re-pattern pattern2) file-path)]
          (when (and prefix suffix)
            {:prefix prefix :suffix suffix})))))

(defn- dest-path
  [file-path]
  (let [separator File/separator]
    (or (when-let [{:keys [prefix suffix]} (match-namespaced-file file-path)]
          (str prefix separator "{{sanitized}}" separator suffix))
        (let [[m] (re-seq #"^gitignore$" file-path)]
          (when m ".gitignore"))
        file-path)))

(defn- excluded-files [_data]
  #{"template.edn"})

(defn- render-templates
  [{:keys [template-dir] :as data}]
  (let [opts        (selmer-opts data)
        render-path (fn [path-template]
                      (selmer/render path-template opts))]
    (->> (file-seq (fs/file template-dir))
         (filter #(and (.isFile %) (not (.isHidden %))))
         (map #(fs/relativize template-dir %))
         (remove #(contains? (excluded-files data) (str %)))
         (map (fn [f]
                {:src-path  (str f)
                 :dest-path (render-path (dest-path (str f)))
                 :output    (helpers/render-selmer
                             (slurp (fs/file template-dir f))
                             opts)
                 :temp-name (str (random-uuid))})))))

(defn data-fn
  [data]
  (assoc data ::template-files (render-templates data)))

(defn- write-temporary-files
  [temp-dir template-files]
  (doseq [{:keys [temp-name output]} template-files]
    (let [content (if (.endsWith output "\n")
                    output
                    (str output "\n"))]
      (spit (fs/file temp-dir temp-name) content))))

(defn- transform-temporary-files
  [temp-dir {:keys [template-dir ::template-files]}]
  (let [extra-dir  (str (fs/relativize template-dir (str temp-dir)))
        rename-map (->> template-files
                        (map (fn [{:keys [temp-name dest-path]}]
                               [temp-name dest-path]))
                        (into {}))]
    [[extra-dir rename-map :only :raw]]))

(defn template-fn
  [template {:keys [::template-files] :as data}]
  (let [temp-dir (-> (fs/create-temp-dir) fs/delete-on-exit)]
    (write-temporary-files temp-dir template-files)
    (assoc template :transform (transform-temporary-files temp-dir data))))
