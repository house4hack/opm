(ns track.model
  (:require [track.validation :as validate]
            [track.database :as database]))

(defprotocol Persistance
  "Interface to the database."
  (store [obj] "Store an object in the database.")
  (update [obj] [obj owner] "Update an (owned) object in the database.")
  (delete [obj] [obj owner] "Delete an (owned) object in the database.")
  (refresh [obj] "Refresh object from database."))

(defprotocol ListItems
  "List items belonging to a user."
  (fetch-devices [user])
  (fetch-series [user])
  (fetch-datapoints
    [series]
    [series start end]
    [series start end resolution]))


(declare map->Device map->User map->Series)

(defrecord Device
    [^Integer device_id
     ^Integer owner
     ^String location]
  Persistance
  (store [device]
    (database/store-record! :Devices device))
  (update [{device_id :device_id :as device} {user_id :user_id}]
    (database/update-record!
     :Devices
     ["device_id = ? and owner = ?" device_id user_id]
     device))
  (delete [{device_id :device_id} {user_id :user_id}]
    (database/delete-record!
     :Devices
     ["device_id = ? and owner = ?" device_id user_id]))
  (refresh [{device_id :device_id}]
    (map->Device
     (first
      (database/query-records
       "SELECT * FROM Devices WHERE device_id = ?" device_id))))
  ListItems
  (fetch-series [{id :device_id owner :owner}]
    (database/query-records
     "SELECT * FROM Series WHERE owner = ? and device = ?" owner id))
  (fetch-datapoints [{id :device_id owner :owner}]
    (database/query-records
     "SELECT * FROM NumberDatapoints
      JOIN Series ON NumberDatapoints.series = Series.series_id
      WHERE owner = ? and device = ? LIMIT 100" owner id)))

(defn create-device
  "Factory function for devices."
  [device]
  (map->Device device))

(defrecord User
    [^Integer user_id
     ^Integer username
     ^Integer password_hash
     ^String name]
  Persistance
  (store [user]
    (database/store-record! :Users user))
  (update [user]
    (database/update-record!
     :Users
     ["user_id = ?" (:user_id user)]
     user))
  (delete [user]
    (database/delete-record!
     :Users
     ["user_id = ?" (:user_id user)]))
  (refresh [{user_id :user_id}]
    (map->User
     (first
      (database/query-records
       "SELECT * FROM Users WHERE user_id = ?" user_id))))
  ListItems
  (fetch-devices [{uid :user_id}]
    (database/query-records
     "SELECT * FROM Devices WHERE owner = ?" uid))
  (fetch-series [{uid :user_id}]
    (database/query-records
     "SELECT * FROM Series WHERE owner = ?" uid)))

(defn create-user
  "Factory function for users."
  [{:keys [username password name] :as user}]
  (when (validate/username username)
    (let [user (-> user
                   (assoc :password_hash (str (hash password)))
                   (dissoc :password)
                   (dissoc :user_id))]
      (map->User user))))

(defrecord Series
    [^Integer series_id
     ^Integer device
     ^String attribute
     ^Integer owner
     ^String unit]
  Persistance
  (store [series]
    (database/store-record!
     :Series
     series))
  (update [{sid :series_id :as series} {uid :user_id}]
    (database/update-record!
     :Series
     ["series_id = ? and owner = ?" sid uid]
     series))
  (delete [{sid :series_id} {uid :user_id}]
    (database/delete-record!
     :Series
     ["series_id = ? and owner = ?" sid uid]))
  (refresh [{id :series_id}]
    (map->Series
     (first
      (database/query-records
       "SELECT * FROM Series WHERE series_id = ?" id))))
  ListItems
  (fetch-datapoints [{sid :series_id} start end resolution]
    (database/query-records
     "SELECT time, measurement FROM NumberDatapoints WHERE series = ? ORDER BY time DESC LIMIT 10"
     sid))
  (fetch-datapoints [{sid :series_id}]
    (database/query-records
     "SELECT time, measurement FROM NumberDatapoints WHERE series = ?"
     sid))
  (fetch-datapoints [{sid :series_id} start end]
    (database/query-records
     "SELECT time, measurement FROM NumberDatapoints
      WHERE series = ? AND time BETWEEN ? AND ?"
     sid start end)))

(defn create-series
  "Factory function for series."
  ([series]
     (map->Series series)))

(defn fetch-series-id
  "Will fetch a series template for a message based on the device (id)
  and the attribute in the message."
  [device attribute]
  (:series_id
   (first (database/query-records
           "SELECT series_id FROM Series WHERE device = ? AND attribute = ?"
           device attribute))))

(defrecord NumberDatapoint
    [series
     time
     measurement]
  Persistance
  (store [datapoint]
    (database/store-record! :NumberDatapoints datapoint)))

(defrecord StringDatapoint
    [series
     time
     measurement]
  Persistance
  (store [datapoint]
    (database/store-record! :StringDatapoints datapoint)))

(defn create-datapoint
  "Datapoint Factory"
  [{:keys [measurement] :as datapoint}]
  (if (string? measurement)
    (map->StringDatapoint datapoint)
    (map->NumberDatapoint datapoint)))
