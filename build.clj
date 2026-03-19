(ns build
  (:require
   [clojure.tools.build.api :as b]
   [deps-deploy.deps-deploy :as deploy]))

(def lib 'io.github.mycelium-clj/mycelium-web-template)
(def version "0.2.3")

(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))

(defn clean [_]
  (b/delete {:path "target"}))

(defn jar [_]
  (clean nil)
  (let [basis (b/create-basis {:project "deps.edn"})]
    (b/write-pom {:class-dir class-dir
                  :lib       lib
                  :version   version
                  :basis     basis
                  :src-dirs  ["src"]
                  :pom-data  [[:licenses
                               [:license
                                [:name "MIT License"]
                                [:url "https://opensource.org/license/mit"]]]]})
    (b/copy-dir {:src-dirs   ["src" "resources"]
                 :target-dir class-dir})
    (b/jar {:class-dir class-dir
            :jar-file  jar-file})))

(defn install [_]
  (jar nil)
  (let [basis (b/create-basis {:project "deps.edn"})]
    (b/install {:basis     basis
                :lib       lib
                :version   version
                :jar-file  jar-file
                :class-dir class-dir})))

(defn deploy [_]
  (jar nil)
  (deploy/deploy {:installer      :remote
                  :sign-releases? false
                  :pom-file       (b/pom-path {:lib lib :class-dir class-dir})
                  :artifact       jar-file}))
