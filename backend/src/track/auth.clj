(ns track.auth
  (:require [track.database :as database])
  (:use [ring.util.response :only [redirect]]
        [track.model :only [map->User]]))

(defn authenticate
  "Function for basic authentication."
  [^String username ^String password]
  (when (and username password)
    (let [query "SELECT * FROM Users WHERE username = ? AND password_hash = ?"
          password_hash (str (hash password))]
      (try
        (map->User
         (first
          (database/query-records query username password_hash )))
        (catch Exception e nil)))))

(defn wrap-https-redirect
  [handler]
  (fn [{:keys [scheme server-name uri] :as req}]
    (if (= :http scheme)
      (redirect (str "https://" server-name ":443" uri))
      (handler req))))
