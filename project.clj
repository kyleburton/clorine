(defproject com.relaynetwork/clorine "1.3.13"
  :description "Clorine"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :jvm-opts ["-Xmx256M"]
  :aot      [rn.clorine.retries-exhausted-exception]
  :lein-release {:deploy-via :clojars}
  :dev-dependencies [[swank-clojure "1.4.2"]
                     [org.xerial/sqlite-jdbc  "3.7.2"]]
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [org.clojure/java.jdbc "0.2.0"]
                 [org.clojars.kyleburton/clj-etl-utils "1.0.53"]
                 [commons-dbcp/commons-dbcp "1.4"]])

