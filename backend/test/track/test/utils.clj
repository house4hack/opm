(ns ^{:doc "Utility functions for testing."
      :author "Mikkel Christiansen"}
  
  track.test.utils
  
  (:use [clojure.java.jdbc :only [create-table drop-table with-connection]]
        [track.database :only [create-connection-pool]])
  (:import [java.io ByteArrayInputStream]))

(defn stream [s]
  (ByteArrayInputStream. (.getBytes s "UTF-8")))

(defn illegal-argument? [clause]
  (= IllegalArgumentException (type clause)))

(defmacro with-private-fns [[ns fns] & tests]
  "Refers private fns from ns and runs tests in context."
  `(let ~(reduce #(conj %1 %2 `(ns-resolve '~ns '~%2)) [] fns)
     ~@tests))
