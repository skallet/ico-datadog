(ns datadog.data
  (:require [clojure.java.io :as io]
            [clojure.string :as s]
            [clojure.data.csv :as csv]
            [clojure.xml :as xml]
            [environ.core :refer [env]]
            [clj-http.client :as c]
            [xml-in.core :as xml-in])
  (:import (javax.xml.parsers SAXParser SAXParserFactory)))

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

(defn startparse-sax
  "Don't validate the DTDs, they are usually messed up."
  [s ch]
  (let [factory (SAXParserFactory/newInstance)]
    (.setFeature factory "http://apache.org/xml/features/nonvalidating/load-external-dtd" false)
    (let [^SAXParser parser (.newSAXParser factory)]
      (.parse parser s ch))))

(defn different-keys? [content]
  (when content
    (let [dkeys (count (filter identity (distinct (map :tag content))))
          n (count content)]
      (= dkeys n))))

(defn xml->json [element]
  (cond
    (nil? element) nil
    (string? element) element
    (sequential? element) (if (> (count element) 1)
                           (if (different-keys? element)
                             (reduce into {} (map (partial xml->json ) element))
                             (map xml->json element))
                           (xml->json  (first element)))
    (and (map? element) (empty? element)) {}
    (map? element) (if (:attrs element)
                    {(:tag element) (xml->json (:content element))
                     (keyword (str (name (:tag element)) "Attrs")) (:attrs element)}
                    {(:tag element) (xml->json  (:content element))})
    :else nil))

(defn ->datamap [nodes]
  (->> nodes
    (map (fn [dom]
           (xml->json (xml-in/find-first dom [:Udaj]))))))

(defn ->map [dom]
  (merge {:udaje (->datamap (xml-in/find-all dom [:Subjekt :udaje]))}
         {:ico (first (xml-in/find-first dom [:Subjekt :ico]))
          :nazev (first (xml-in/find-first dom [:Subjekt :nazev]))
          :zapisDatum (first (xml-in/find-first dom [:Subjekt :zapisDatum]))}))

(defn get-data-seq [file]
  (map ->map
       (-> file
           io/input-stream
           (xml/parse startparse-sax)
           (xml-in/find-all [:xml]))))
