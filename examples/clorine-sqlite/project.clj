(defproject clorine-sqlite "1.0.0-SNAPSHOT"
  :description "Clorine with SQLite"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :jvm-opts ["-Xmx256M"]
  :dev-dependencies [[swank-clojure "1.4.2"]]
  :dependencies [[org.clojure/clojure "1.3.0"]
  [org.xerial/sqlite-jdbc  "3.7.2"]
                 [com.relaynetwork/clorine "1.3.12-SNAPSHOT"]])
