(ns ^{:doc "Utility functions for the database."
      :author "Mikkel Christiansen"}
  
  track.database
  
  (:use [clojure.java.jdbc :rename {resultset-seq resultset}])
  (:import [com.mchange.v2.c3p0 ComboPooledDataSource DataSources]))

(defonce connection-string
  (or (System/getenv "JDBC_CONNECTION_STRING")
      "jdbc:mysql://127.0.0.1:3306/machinemonitor"))

(defn create-connection-pool
  "To improve the database responsiveness we use a connection pool."
  []
  (let [cpds (doto (ComboPooledDataSource.)
               (.setDriverClass "com.mysql.jdbc.Driver")
               (.setJdbcUrl connection-string)
               (.setUser "beowulf")
               (.setPassword "grindel")
               (.setMaxIdleTimeExcessConnections (* 30 60))
               (.setMaxIdleTime (* 3 60 60))
               (.setLogWriter (java.io.PrintWriter. *out*)))]
    {:datasource cpds}))


(defonce cpds (create-connection-pool))


(defn destroy-connection-pool []
  (try
    (DataSources/destroy (:datasource cpds))
      (catch java.sql.SQLException e
        (println e))))


(defn store-record! [table record]
  "Store a record in a given database and table."
  (with-connection cpds
    (insert-record table record)))

(defn update-record!
  "Update a record in a given table based on id."
  [table where-params record]
  (with-connection cpds
    (update-values table where-params record)))

(defn delete-record!
  "Update a record in a given table based on id."
  [table where-params]
  (with-connection cpds
    (delete-rows table where-params)))

(defn query-records
  ([query]
     (with-connection cpds
       (with-query-results res [query]
         (doall res))))
  ([query & params]
     (with-connection cpds
       (with-query-results res (vec (cons query params))
         (doall res)))))
