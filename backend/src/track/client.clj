(ns track.client
  (:require '[clj-http.client :as client]))


(defn send-sms
  [message number]
  (client/post "http://site.com/api"
               {:basic-auth ["user" "pass"]
                :body "{\"json\": \"input\"}"
                :headers {"X-Api-Version" "2"}
                :content-type :json
                :socket-timeout 1000
                :conn-timeout 1000
                :accept :json}))
