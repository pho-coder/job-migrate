(defproject job-migrate "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [clj-zookeeper "0.3.0-SNAPSHOT"]
                 [com.jd.bdp.magpie/magpie-utils "0.1.3-SNAPSHOT"]
                 [org.clojure/tools.cli "0.3.5"]
                 [org.clojure/tools.logging "0.3.1"]
                 [ch.qos.logback/logback-classic "1.1.7"]]
  :main ^:skip-aot job-migrate.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all}})
