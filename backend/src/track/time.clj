(ns track.time
  (:use clj-time.coerce))


(defn string->sql-timestamp [date]
  (to-timestamp (from-string date)))
