(defproject track "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [org.clojure/java.jdbc "0.2.1"]
                 [org.clojure/data.json "0.1.2"]
                 [org.clojure/tools.logging "0.2.3"]
                 [mysql/mysql-connector-java "5.1.18"]
                 [c3p0/c3p0 "0.9.1.2"]
                 [ring/ring-core "1.1.0"]
                 [mschristiansen/moustache "1.1.0"]
                 [ring-basic-authentication "1.0.1"]
                 [hiccup "1.0.0"]]
  :dev-dependencies [[ring/ring-jetty-adapter "1.1.0"]
                     [ring/ring-devel "1.1.0"]
                     [ring/ring-servlet "1.1.0"]
                     [lein-beanstalk "0.2.2"]]
  :resources-path "resources"
  :ring {:handler track.core/routes
         :destroy track.database/destroy-connection-pool
         :servlet-name track
         :url-pattern "/*"
         ;; If true, a :path-info key is added to the request map.
         :servlet-path-info? true})
