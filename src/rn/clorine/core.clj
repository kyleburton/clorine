(ns
    ^{:doc "Clorine: Purified Database Connection Pool Management"
      :authors "Kyle Burton <kyle.burton@gmail.com>, Paul Santa Clara, Josh Crean"}
  rn.clorine.core
  (:require [clojure.pprint                         :as pp]
            [clojure.java.jdbc.deprecated           :as jdbc]
            [clojure.string                         :as str-utils])
  (:import [org.apache.commons.dbcp  BasicDataSource]
           [rn.clorine RetriesExhaustedException]))

(defonce
  ^{:doc  "Package level connection info registry."
    :added "1.0.0"}
  connection-registry (ref {}))

(def
 ^{:doc "Thread local mapping of registered database configuration name to opened connection."
   :added "1.0.0"
   :dynamic true}
 *curr-thread-connections* nil)

(defn retries-exhausted-get-errors [errors]
  (str-utils/join
   "\n"
   (map #(.getMessage ^Throwable %1) errors)))

(defn get-connection [conn-name]
  (if-not (contains? @connection-registry conn-name)
    (throw (IllegalArgumentException. (format "Error: connection name not registered: %s the following are registered: %s"
                                              conn-name
                                              (vec (keys @connection-registry))))))
  (if-let [conn (get @*curr-thread-connections* conn-name)]
    ;; [conn we-opened-it]
    [conn false]
    (let [new-connection (.getConnection #^BasicDataSource (get @connection-registry conn-name))]
      (swap! *curr-thread-connections* assoc conn-name new-connection)
      [new-connection true])))

(defn get-datasource [conn-name]
  (if-not (contains? @connection-registry conn-name)
    (throw (IllegalArgumentException. (format "Error: connection name not registered: %s the following are registered: %s"
                                              conn-name
                                              (vec (keys @connection-registry))))))
  (if-let [conn (get @*curr-thread-connections* conn-name)]
    [conn false]
    [(get @connection-registry conn-name) true]))

(def ^{:dynamic true} *datasource* nil)

(defn with-datasource* [conn-name func]
  (let [helper-fn
        #(let [[ds we-opened-it] (get-datasource conn-name)]
           (binding [*datasource* {:datasource ds}]
             (func)))]
    (if (nil? *curr-thread-connections*)
      (binding [*curr-thread-connections* (atom {})]
        (helper-fn))
      (helper-fn))))

(defn with-connection* [conn-name func]
  (let [helper-fn
        #(let [[conn we-opened-it] (get-connection conn-name)]
           (binding [jdbc/*db*
                     (if we-opened-it
                       {:connection conn
                        :level       0
                        :rollback   (atom false)}
                       ;; NB: known bug: if we have nested
                       ;; with-connection*'s, eg: (wc :foo ... (wc
                       ;; :bar ... (wc :foo ...)))  in that case, the
                       ;; 2nd :foo should be the same as the first
                       ;; but since we-opened-it is false, we'll be
                       ;; using the current value of
                       ;; #'clojure.java.jdbc/*db*, which will be :bar
                       ;; and is _WRONG_.
                       (var-get #'jdbc/*db*))]
             (try
              (func)
              (finally
               (if we-opened-it
                 (do
                   (swap! *curr-thread-connections* dissoc conn-name)
                   (.close ^java.sql.Connection conn)))))))]
    (if (nil? *curr-thread-connections*)
      (binding [*curr-thread-connections* (atom {})]
        (let [res (helper-fn)]
          (when-not (empty? @*curr-thread-connections*)
            (throw (java.lang.RuntimeException. (format "Error: at top level, *curr-thread-connections* is non-emtpy: %s " @*curr-thread-connections*))))
          res))
      (helper-fn))))


(defmacro with-connection [conn-name & body]
  `(with-connection* ~conn-name (fn [] ~@body)))

(defn- test-connection [name]
  (with-connection name true))


(defn register-connection! [name params]
  (Class/forName (:driver-class-name params))
  (let [connection-pool (doto (BasicDataSource.)
                          (.setDriverClassName (:driver-class-name params))
                          (.setUsername        (:user params))
                          (.setPassword        (:password params))
                          (.setUrl             (:url params))
                          (.setMaxActive       (:max-active params 8))
                          (.setDefaultReadOnly (:read-only params false))
                          (.setMaxWait         (:max-wait params 0))
                          (.setAccessToUnderlyingConnectionAllowed (:access-underlying-connection params false))
                          (.setTestOnBorrow    (:test-on-borrow params false))
                          (.setTestOnReturn    (:test-on-return params false))
                          (.setTestWhileIdle   (:test-while-idle params false))
                          (.setValidationQuery (:validation-query params))
                          (.setTimeBetweenEvictionRunsMillis (:time-between-eviction-runs-millis params -1))
                          (.setMinEvictableIdleTimeMillis    (:min-evictable-idle-time-millis params (* 1000 60 30)))
                          (.setNumTestsPerEvictionRun        (:num-tests-per-eviction-run params 3))) ]
    (dosync
     (alter connection-registry assoc name connection-pool))))


(defn with-retry* [num-retries retryable-error? body-fn]
  (loop [retries-left num-retries
         errors []]
    (if (zero? retries-left)
      (throw (rn.clorine.RetriesExhaustedException.
              (.concat "Retries Exhausted. :: "
                       (retries-exhausted-get-errors errors))
              errors))
      (let [ex     (atom nil)
            result (atom nil)]
        (try
         (reset! result (body-fn))
         (catch Exception e
           (reset! ex e)))
        (cond (not @ex)
              @result

              (retryable-error? @ex)
              (recur (dec retries-left) (conj errors @ex))

              :else
              (throw (rn.clorine.RetriesExhaustedException.
                      (format "UnRetriable Exception encoutered. %s/%s :: %s"
                              (class @ex)
                              (.getMessage ^Throwable @ex)
                              (retries-exhausted-get-errors errors))
                      @ex
                      errors)))))))


(defmacro with-retry [num-retries exception-predicate & body]
  `(with-retry* ~num-retries ~exception-predicate (fn [] ~@body) ))
