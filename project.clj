(defproject deploy-jenkins-dsl "0.1.0"
  :description "Simple program to deploy jenkins jobs dsl to the job server"
  :url ""
  :min-lein-version "2.0.0"
  :aot :all
  :main deploy-jenkins-dsl.core
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [org.codehaus.groovy/groovy-all "2.3.9"]
                 [me.raynes/fs "1.4.6"]
                 [hu.rxd/job-dsl-core "1.20-rxd-3"]
                 [clj-yaml "0.4.0"]
                 [jenkins "0.1.0-SNAPSHOT"]
                 [shoreleave "0.3.0"]
                 [shoreleave/shoreleave-remote "0.3.0"]
                 [shoreleave/shoreleave-remote-ring "0.3.0"]
                 [org.clojure/tools.cli "0.3.1"]
                 ]
  :plugins [[lein-ancient "0.5.4"]
;;            [org.thelastcitadel/jenkins-clojure-injector "0.2.1"]
            ]
  :jenkins-inject deploy-jenkins-dsl.core/main)
