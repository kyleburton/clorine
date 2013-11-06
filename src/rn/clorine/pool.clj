(ns rn.clorine.pool
  (:import
   [org.apache.commons.pool PoolableObjectFactory]
   [org.apache.commons.pool.impl GenericObjectPool]))


(defonce *registry* (atom {}))

(defmacro set-if-contains [opts kname pool meth]
  `(if (contains? ~opts ~kname)
     (~meth ~pool (~kname ~opts))))

(defn register-pool
  ([pool-name factory-impl]
     (register-pool pool-name factory-impl {}))
  ([pool-name factory-impl opts]
     (let [pool (GenericObjectPool. factory-impl)]
       (set-if-contains opts :max-active pool .setMaxActive)
       (set-if-contains opts :lifo       pool .setLifo)
       (set-if-contains opts :max-idle   pool .setMaxIdle)
       (set-if-contains opts :max-wait   pool .setMaxWait)
       (set-if-contains opts :min-idle   pool .setMinIdle)
       (swap! *registry*
              assoc pool-name
              {:name    pool-name
               :factory factory-impl
               :pool    pool}))))

(defn unregister-pool [pool-name]
  (swap! *registry*
         dissoc pool-name))

(defn make-factory [factory-fns-map]
  (if-not (contains? factory-fns-map :make-fn)
    (throw (RuntimeException. "Error: you must supply at least a :make-fn, but you can supply any of: [:activate-fn :destroy-fn :passivate-fn :validate-fn]")))
  (let [no-op (fn [this #^Object obj] nil)
        factory-fns-map (merge {:activate-fn  no-op
                                :destroy-fn   no-op
                                :passivate-fn no-op
                                :validate-fn  no-op}
                               factory-fns-map)
        {make-fn :make-fn activate-fn :activate-fn destroy-fn :destroy-fn passivate-fn :passivate-fn validate-fn :validate-fn} factory-fns-map]
    (reify
     org.apache.commons.pool.PoolableObjectFactory
     (#^void activateObject [this #^Object obj]
             (activate-fn this obj))
     (#^void destroyObject [this #^Object obj]
             (destroy-fn this obj))
     (#^Object makeObject [this]
               (make-fn this))
     (#^void passivateObject [this #^Object obj]
             (passivate-fn this obj))
     (#^boolean validateObject [this #^Object obj]
                (passivate-fn obj)))))

(defn get-registered-pool [pool-name]
  (@*registry* pool-name))

(defn with-instance* [pool-name body-fn]
  (let [registered-pool         (@*registry* pool-name)
        pool                    (:pool registered-pool)
        instance (.borrowObject pool)]
    (try
     (body-fn instance)
     (finally
      (.returnObject pool instance)))))

(defmacro with-instance [[inst-name pool-name] & body]
  `(with-instance* ~pool-name
     (fn [~inst-name]
       ~@body)))

