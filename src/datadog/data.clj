(ns datadog.data
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.data.csv :as csv]
            [environ.core :refer [env]]
            [clj-http.client :as c]))

(defn link->name [link]
  (last
    (s/split link #"\/")))

(defn line->list [l]
  (first
    (csv/read-csv l
                  :separator \,
                  :quote \")))

(defn data-path [file]
  (str (or (env :data-folder)
           "resources")
       (java.io.File/separator)
       file))

(defn get-data [link & {:keys [to-output]}]
  (with-open [w (io/output-stream (or to-output "link-data"))]
    (io/copy (:body (c/get link {:as :stream}))
             w)))

(defn remove-data [file]
  (io/delete-file file))

(defn get-links []
  (with-open [w (io/reader (or (env :link-file) "resources/links.txt"))]
    (doall (line-seq w))))

(defn get-header [file]
  (with-open [w (io/reader file)]
    (->> w
      (line-seq)
      (first)
      (line->list)
      (map keyword))))

(defn get-data-seq [file cb]
  (with-open [w (io/reader file)]
    (doseq [l (rest (line-seq w))]
      (cb l))))
