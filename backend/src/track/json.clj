(ns track.json
  (:require [track.database :as database])
  (:use [clojure.data.json]
        [track.model :only [create-datapoint refresh create-device fetch-series-id store create-series fetch-series fetch-datapoints]])
  (:import [java.io PrintWriter EOFException]))

(defn response
  "Standard JSON response."
  [data & [^Integer status]]
  {:status (or status 200)
   :headers {"Content-Type" "application/json"}
   :body (json-str data)})

(defn error-code [e]
  (condp = (type e)
      IllegalArgumentException 400
      NullPointerException 400
      EOFException 404
      Exception 400))

(defn wrap-error-handling [handler]
  (fn [req]
    (try
      (handler req)
      (catch RuntimeException e
         (.. e getCause getMessage))
      (catch Exception e
        (response {:error (.getMessage e)} 200)))))

(defn- json-request?
  [req]
  (if-let [type (:content-type req)]
    (not (empty? (re-find #"^application/(vnd.+)?json" type)))))

(defn wrap-json-params [handler]
  (fn [req]
    (if-let [body (and (json-request? req) (:body req))]
      (let [bstr (slurp body)
            json-params (read-json bstr)
            req* (assoc req
                   :json-params json-params
                   :params (merge (:params req) json-params))]
        (handler req*))
      (handler req))))

;;; Copied from clojure.data.json to extend the Write-JSON protocol
;;; to java.sql.Timestamp
(defn- write-json-string [^CharSequence s ^PrintWriter out escape-unicode?]
  (let [sb (StringBuilder. ^Integer (count s))]
    (.append sb \")
    (dotimes [i (count s)]
      (let [cp (Character/codePointAt s i)]
        (cond
         ;; Handle printable JSON escapes before ASCII
         (= cp 34) (.append sb "\\\"")
         (= cp 92) (.append sb "\\\\")
         (= cp 47) (.append sb "\\/")
         ;; Print simple ASCII characters
         (< 31 cp 127) (.append sb (.charAt s i))
         ;; Handle non-printable JSON escapes
         (= cp 8) (.append sb "\\b")
         (= cp 12) (.append sb "\\f")
         (= cp 10) (.append sb "\\n")
         (= cp 13) (.append sb "\\r")
         (= cp 9) (.append sb "\\t")
	 ;; Any other character is Unicode
         :else (if escape-unicode?
		 ;; Hexadecimal-escaped
		 (.append sb (format "\\u%04x" cp))
		 (.appendCodePoint sb cp)))))
    (.append sb \")
    (.print out (str sb))))

(defn- write-json-timestamp
  "Will convert a Java Timestamp into a string for the Write-JSON protocol."
  [x out escape-unicode?]
  (write-json-string (str x) out escape-unicode?))

(extend java.sql.Timestamp
  Write-JSON
  {:write-json write-json-timestamp})

(def default-start (java.sql.Timestamp. 0))
(def default-end (java.sql.Timestamp. (.getTime (java.util.Date.))))


(defn add-timestamp
  "TODO: Add timestamp to all incoming measurements."
  [message]
  (if (contains? message :time)
    message
    (assoc message :time "TODO:")))

(defn- divide-measurements
  "Function to split a device message into one message per attribute."
  [{device-id :device :as message}]
  (let [attributes (keys (dissoc message :device))]
    (for [attr attributes]
      {:device device-id
       :attribute (name attr)
       :measurement (attr message)})))

(defn- measurement->datapoint
  "Function to assign each datapoint a series id."
  [{:keys [device attribute time measurement]}]
  (let [owner (:owner (refresh (create-device {:device_id device})))
        sid (or (fetch-series-id device attribute)
                (:generated_key
                 (store (create-series {:device device
                                        :attribute attribute
                                        :owner owner}))))]
    (create-datapoint {:series sid
                       :time time
                       :measurement measurement})))

(defn store-measurements
  "This function"
  [message]
  (let [messages (divide-measurements message)]
    (response (map #(store (measurement->datapoint %)) messages))))


(defn fetch-measurements
  [series-id {:strs [start end]}]
  (let [series (create-series {:series_id series-id})]
    (response (cond
               (and (nil? start) (nil? end)) (fetch-datapoints series)
               (nil? start) (fetch-datapoints series default-start end)
               (nil? end) (fetch-datapoints series start default-end)))))

(defn list-series
  [{user :basic-authentication}]
  (response (fetch-series user)))
