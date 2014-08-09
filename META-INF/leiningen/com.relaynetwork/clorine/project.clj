(defproject com.relaynetwork/clorine "1.0.14"
  :description  "Clorine"
  :url          "http://github.com/relaynetwork/clorine"
  :lein-release {:deploy-via :clojars}
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}
  :repositories         {"sonatype-oss-public" "https://oss.sonatype.org/content/groups/public/"}
  :aot      [rn.clorine.retries-exhausted-exception]
  :local-repo-classpath true
  :plugins [[lein-release/lein-release "1.0.4"]]
  :profiles             {:dev {:dependencies [[swank-clojure "1.4.3"]
                                              [org.xerial/sqlite-jdbc  "3.6.20"]
                                              ]}
                         :1.2 {:dependencies [[org.clojure/clojure "1.2.0"]]}
                         :1.3 {:dependencies [[org.clojure/clojure "1.3.0"]]}
                         :1.4 {:dependencies [[org.clojure/clojure "1.4.0"]]}
                         :1.5 {:dependencies [[org.clojure/clojure "1.5.1"]]}
                         :1.6 {:dependencies [[org.clojure/clojure "1.6.0-master-SNAPSHOT"]]}}
  :aliases              {"all" ["with-profile" "dev,1.2:dev,1.3:dev,1.4:dev,1.5:dev,1.6"]}
  :global-vars          {*warn-on-reflection* true}
  :dependencies         [
                         [org.clojure/java.jdbc     "0.2.3"]
                         [commons-dbcp/commons-dbcp "1.4"]])
