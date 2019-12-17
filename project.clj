(defproject datadog "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/java.jdbc "0.7.10"]
                 [org.clojure/data.csv "0.1.4"]
                 [org.clojure/data.json "0.2.7"]
                 [org.clojure/data.xml "0.0.8"]
                 [mysql/mysql-connector-java "5.1.6"]
                 [clj-http "3.10.0"]
                 [clj-time "0.15.2"]
                 [environ "1.1.0"]
                 [tolitius/xml-in "0.1.0"]]
  :main ^:skip-aot datadog.core
  :target-path "target/%s"
  :profiles
  {:uberjar {:aot :all}
   :dev [:project/dev :profiles/dev]
   :profiles/dev  {:env {:db-type "mysql"
                         :db-name "datadog_dev"
                         :db-host "localhost"
                         :db-user "root"
                         :db-pass "root"}}
   :project/dev {:dependencies [[proto-repl "0.3.1"]]
                 :plugins [[lein-environ "1.1.0"]]}})
