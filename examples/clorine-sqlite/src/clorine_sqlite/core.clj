(ns clorine-sqlite.core
  (:require
   [rn.clorine.core   :as cl]
   [clojure.java.jdbc :as sql]))

(if (.exists (java.io.File. "sample.db"))
  (.delete (java.io.File. "sample.db")))

(cl/register-connection!
 :inventory-database
 {:driver-class-name "org.sqlite.JDBC"
  :url               "jdbc:sqlite:sample.db"})


(cl/with-connection :inventory-database
  (sql/do-commands
   "CREATE TABLE items (id          integer primary key autoincrement, --
                        sku         varchar(255), --
                        description varchar(255), --
                        quantity    integer);"))

(cl/with-connection :inventory-database
  (sql/do-prepared
   "INSERT INTO items (sku, description, quantity) VALUES (?, ?, ?);"
   [(str (java.util.UUID/randomUUID)) "Flux Capacitor" 0]
   [(str (java.util.UUID/randomUUID)) "Bistromath" 0]
   [(str (java.util.UUID/randomUUID)) "20cm Lens For Heuristically programmed Algorithmic computer, 160-degree FOV" 0]))

(cl/with-connection :inventory-database
  (sql/with-query-results
    rows
    ["SELECT * FROM items"]
    (vec rows)))

(cl/with-connection :inventory-database
  (let [c1 (var-get #'clojure.java.jdbc/*db*)]
    (cl/with-connection :inventory-database
      (let [c2 (var-get #'clojure.java.jdbc/*db*)]
        ;; Assert that these are indeed the same connection
        (if-not (= c1 c2)
          (throw (RuntimeException. (format "Error, expected c1:%s and c2:%s to match"
                                            c1 c2))))))))


