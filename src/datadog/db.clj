(ns datadog.db
  (:require [clojure.java.jdbc :as j]
            [clj-time.format :as f]
            [clj-time.local :as l]
            [clojure.data.json :as json]
            [environ.core :refer [env]]))

(def mysql-db {:dbtype (env :db-type)
               :dbname (env :db-name)
               :user (env :db-user)
               :password (env :db-pass)})

(def built-in-formatter (f/formatters :basic-date-time-no-ms))

(defn file-stamp []
  (f/unparse built-in-formatter (l/local-now)))

(defn now [] (new java.util.Date))


; (j/query mysql-db "select * from sources")

(defn get-subject-from-db [ico]
  (first
    (j/query mysql-db
             ["select * from `sources` where ico = ?" ico])))

(defn get-link-by-id [id]
  (first
    (j/query mysql-db
             ["select * from `sources` where id = ?" id])))

(defn insert-new-source! [data source]
  (->
    (j/insert! mysql-db
               :sources
               {:utime (now)
                :ico (:ico data)
                :data (json/write-str data)
                :source source})
    first
    :generated_key
    get-link-by-id))

(defn update-existing-source! [{:keys [id]} data]
  (j/update! mysql-db
             :sources
             {:data (json/write-str data)
              :utime (now)}
             ["id = ?" id]))

(defn insert-new-diff! [data added removed]
  (->
    (j/insert! mysql-db
               :diffs
               {:source_id (:id data)
                :added (when-not (empty? added) (json/write-str added))
                :removed (when-not (empty? removed) (json/write-str removed))})
    first
    :generated_key))
