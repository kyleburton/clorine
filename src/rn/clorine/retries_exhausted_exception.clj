(ns rn.clorine.retries-exhausted-exception
  (:require
   [clojure.string :as string])
  (:gen-class
   :name         rn.clorine.RetriesExhaustedException
   :extends      Exception
   :prefix       retries-exhausted-ex-
   :constructors {[String java.util.List]
                  [String]
                  [String Throwable java.util.List]
                  [String Throwable]}
   :init         init
   :state        errors))

(defn retries-exhausted-ex-init
  ([#^String msg #^java.util.List exceptions]
     [[msg] exceptions])
  ([#^String msg #^Throwable ex #^java.util.List exceptions ]
     [[msg ex] exceptions]))

(defn retries-exhausted-ex-toString [this]
  (string/join "; "
               (cons (.getMessage this)
                     (map
                      (fn [ctr err]
                        (str ctr ": " (.getMessage err)))
                      (iterate inc 1)
                      (.errors this)))))


(comment

  (def *chicken* (atom nil))

  (let [errors [(RuntimeException. "blah") (RuntimeException. "lunch ")]]
    (reset! *chicken*
            (rn.clorine.RetriesExhaustedException.
             "Error"
             errors)))


  (str @*chicken*)
  (.printStackTrace @*chicken*)





  (compile 'rn.clorine.retries-exhausted-exception)

  )