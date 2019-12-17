(ns datadog.core
  (:gen-class)
  (:require [clojure.data.json :as json]
            [clojure.data :refer [diff]]
            [datadog.data :refer [get-links
                                  get-data
                                  remove-data
                                  data-path
                                  link->name
                                  get-data-seq]]
            [datadog.db :refer [get-subject-from-db
                                insert-new-source!
                                insert-new-diff!]]))

(defn store-diff! [entity new-data source]
  (let [old-data (json/read-str (:data entity)
                            :key-fn keyword)
        [old-added old-removed _] (diff new-data old-data)]
    (comment
      "Data reconstruction"
      (prn (merge old-removed
                  (apply dissoc (into [new-data]
                                      (keys old-added))))))
    (when-not (and (empty? old-added)
                   (empty? old-removed))
      (insert-new-diff! entity
                        old-added
                        old-removed))))

(defn store-new! [data source]
  (insert-new-source! data source))

(defn update-source! [& {:keys [source
                                data]}]
  (try
    (let [ico (read-string (:ico data))]
      (when ico
        (if-let [existing-entity (get-subject-from-db ico)]
          (store-diff! existing-entity
                       data
                       source)
          (store-new! data source))))
    (catch Exception e
      (prn "")
      (prn "Exception:")
      (prn "")
      (prn (.getMessage e)))))
      ; (throw (Exception. "Cannot process: " (str data))))))

(defn process-file! [url-source data-source]
  (doseq [data (get-data-seq data-source)]
    (prn "ICO: " (:ico data))
    (update-source! :source url-source
                    :data-source data-source
                    :data data)))

(comment
  (process-file! (first (get-links))
                 (data-path (link->name (first (get-links))))))

(defn process-links! []
  (doseq [source (get-links)]
    (let [tmp-output (data-path (link->name source))]
      (prn "Processing: " source)
      (get-data source :to-output tmp-output)
      (process-file! source tmp-output)
      (remove-data tmp-output))))

(defn -main
  "Datadog main process function"
  [& args]
  (println "Starting [DataDOG]")
  (process-links!))
