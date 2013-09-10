(defproject com.relaynetwork/clorine "1.0.14-SNAPSHOT"
  :description "Clorine"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :jvm-opts ["-Xmx512M"]
  :aot      [rn.clorine.retries-exhausted-exception]
  :local-repo-classpath true
  :lein-release     {:deploy-via :clojars :scm :git}
  :dev-dependencies [[swank-clojure "1.4.0-SNAPSHOT"]
                     [org.xerial/sqlite-jdbc  "3.6.20"]]
  :dependencies [[org.clojure/clojure "1.2.1"]
                 [org.clojure/clojure-contrib "1.2.0"]
                 [org.clojars.kyleburton/clj-etl-utils "1.0.53"]
                 [commons-dbcp/commons-dbcp "1.4"]])

