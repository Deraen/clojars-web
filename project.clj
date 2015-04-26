(defproject clojars-web "0.15.14-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.cli "0.2.1"]
                 [org.apache.maven/maven-model "3.0.4"
                  :exclusions
                  [org.codehaus.plexus/plexus-utils]]
                 [com.cemerick/pomegranate "0.0.13"
                  :exclusions
                  [org.apache.httpcomponents/httpcore]]
                 [s3-wagon-private "1.0.0"]
                 [metosin/compojure-api "0.20.0"]
                 [metosin/ring-http-response "0.6.1"]
                 ; Friend brings in old version
                 [org.clojure/core.cache "0.6.4"]
                 ; Clj-pgp depends on old version but potemkin requires a new version
                 [byte-streams "0.2.0"]
                 [ring/ring-jetty-adapter "1.3.2"]
                 [hiccup "1.0.5"]
                 [cheshire "5.4.0"]
                 [korma "0.3.0-beta10"]
                 [org.clojars.ato/nailgun "0.7.1"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 [org.apache.commons/commons-email "1.3.3"]
                 [commons-codec "1.6"]
                 [net.cgrand/regex "1.0.1"
                  :exclusions [org.clojure/clojure]]
                 [clj-time "0.9.0"]
                 [com.cemerick/friend "0.2.1"]
                 [clj-stacktrace "0.2.8"]
                 [ring/ring-anti-forgery "1.0.0"]
                 [valip "0.2.0"]
                 [clucy "0.3.0"]
                 [org.clojure/tools.nrepl "0.2.3"]
                 [org.bouncycastle/bcpg-jdk15on "1.47"]
                 [mvxcvi/clj-pgp "0.8.0"]]
  :profiles {:dev {:dependencies [[kerodon "0.0.7"]
                                  [nailgun-shim "0.0.1"]]
                   :resource-paths ["local-resources"]}}
  :plugins [[lein-ring "0.8.5"]]
  :aliases {"migrate" ["run" "-m" "clojars.db.migrate"]}
  :ring {:handler clojars.web/clojars-app}
  :aot [clojars.scp]
  :main clojars.main
  :min-lein-version "2.0.0")
