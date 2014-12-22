(ns deploy-jenkins-dsl.core
  ^ {:author "Matthew Burns"
     :doc "Top level handler that creates jobs for a application project from groovy dsl files,
           copies those files to a directory"}
    (:import [java.io.File]
             [org.codehaus.groovy.control CompilerConfiguration]
             [groovy.lang GroovyClassLoader Binding]
             [groovy.util GroovyScriptEngine]
             [org.codehaus.groovy.control.customizers ImportCustomizer]
             [javaposse.jobdsl Run]
             [javaposse.jobdsl.dsl ScriptRequest GeneratedJob DslScriptLoader JobType])
    (:require [clojure.core :refer [re-find]]
              [clojure.java.io :as io]
              [clojure.xml :as xml]
              [clojure.pprint :refer [pprint pp]]
              [me.raynes.fs :refer [mkdirs]]
              [shoreleave.server-helpers :refer [safe-read]]
              [clojure.tools.cli :refer [parse-opts]]
              [clj-yaml.core :as yaml]
              [jenkins.core :refer [jobs job build with-config builds build! job-config jobs-by-color failing-jobs
                                    last-successful-build last-build]])
    (:gen-class))

(defn read-config
  "Read a config file and return it as Clojure Data.  Usually, this is a hashmap"
  ([]
     (read-config (str (System/getProperty "user.dir") "/resources/config.edn")))
  ([config-loc]
     (safe-read (slurp config-loc))))

(def config (read-config))

(defn read-lines [filename]
  (let [rdr (io/reader filename)]
    (defn read-next-line []
      (if-let [line (.readLine rdr)]
        (cons line (lazy-seq (read-next-line)))
        (.close rdr)))
    (lazy-seq (read-next-line))))

(defn regex-file-seq
  "Lazily filter a directory based on a regex."
  [re dir]
  (filter #(re-find re (.getPath %)) (file-seq dir)))

(defn copy-file
  "TODO: remove and replace with me.raynes.fs"
  [source-file dest-path]
  (io/copy source-file (io/file dest-path)))

(defn copy-job-promotion-dsl-files
  "Move and rename the dsl-files to their correct directory in .jenkins/jobs/[job]/"
  [{:keys [project-name file-list dest-path] :as args}]
  (let [tuples (map (fn [f]
                      (let [dir-name (clojure.string/replace (.getName f) #"\.xml" "")
                            [file-name env promo-name] (nth (re-seq #"^.*_(\w+)-promotions-(.*)\.xml" (.getName f)) 0)]
                        [f env promo-name])) file-list)]
    (doseq [t tuples]
      (let [f (nth t 0)
            env (nth t 1)
            promo-name (nth t 2)
            dir-name (clojure.string/replace (.getName f) #"\.xml" "")
            dir-path (format "%s/%s_%s/%s/%s" dest-path project-name env "promotions"  promo-name)
            _ (me.raynes.fs/mkdirs  dir-path)
            ;;            _ (io/make-parents (java.io.File.  dir-path))
            ]
        (copy-file f (format "%s/config.xml" dir-path))))))

(defn copy-job-dsl-files
  "Move and rename the dsl-files to their correct directory in .jenkins/jobs/[job]/config"
  [file-list dest-path]
  (doseq [f file-list]
    (let [dir-name (clojure.string/replace (.getName f) #"\.xml" "")
          dir (me.raynes.fs/mkdirs (format "%s/%s" dest-path dir-name))]
      (copy-file f (format "%s/%s/config.xml" dest-path dir-name)))))

(defn -deploy
  "Driver function creating the jobs for projects and the promotions those jobs contain"
  [{:keys [ dsl-dir-path jobs-dir-path project-name] :as args}]
  (let [dsl-dir (clojure.java.io/file dsl-dir-path)
        regex (re-pattern (str project-name ".*\\.xml"))
        xml-files (regex-file-seq regex dsl-dir)
        pf (filter (fn [f] (re-matches #".*-promotions.*\.xml" (.getName f))) xml-files)
        jf (remove (fn [f] (re-matches #".*-promotions.*\.xml" (.getName f))) xml-files)]
    (do
      (copy-job-dsl-files jf jobs-dir-path)
      (copy-job-promotion-dsl-files {:project-name project-name :file-list pf :dest-path jobs-dir-path}))
    xml-files))

(defn trigger-seed-job
  "Assuming there is a job dsl responsible for starting jobs correctly
   TODO add config parameters as keywords"
  [job-name]
  (let [conf {:jenkins-url "http://localhost:8080" :username "" :password ""}
        seed_job (with-config conf (job job-name))
        _ (with-config conf (build! job-name))
        build-results  (with-config conf (last-build job-name))]
    {:job seed_job :results build-results}))

(defn process [prj]
  "driver entry point"
  (let [opts {:dsl-dir-path  (:dsl-dir-path config)
              :jobs-dir-path (:jobs-dir-path config)
              :project-name prj
              }
        job-cfg-deploy-results (-deploy opts)]
;;    (trigger-seed-job "verified_stg")
    ))

(def modules (:modules config))

(defn -main [& args]
  (doseq [m modules]
    (process m)))

(comment
  (-main)
  )
