(ns clojars.routes.api
  (:require [clojure.set :refer [rename-keys]]
            [schema.core :as s]
            [compojure.api.sweet :refer :all]
            [compojure.route :refer [not-found]]
            [ring.util.http-response :refer [ok]]
            [clojars.db :as db]
            [clojars.web.common :as common]
            [clojars.stats :as stats]
            [korma.core :refer [exec-raw]]))

(defn get-artifact [group-id artifact-id]
  (let [stats (stats/all)]
    (some-> (db/find-jar group-id artifact-id)
            (dissoc :id :created :promoted_at)
            (assoc :recent_versions (db/recent-versions group-id artifact-id)
                   :downloads (stats/download-count stats group-id artifact-id))
            (update-in [:recent_versions] (fn [versions]
                                            (map (fn [version]
                                                   (assoc version :downloads (stats/download-count stats group-id artifact-id (:version version))))
                                                 versions))))))

(defn jars-by-groupname [groupname]
    (exec-raw [(str
              "select j.*, j2.version as latest_release "
              "from jars j "
              ;; Find the latest version
              "join "
              "(select jar_name, max(created) as created "
              "from jars "
              "group by group_name, jar_name) l "
              "on j.jar_name = l.jar_name "
              "and j.created = l.created "
              ;; Find the latest release
              "join "
              "(select jar_name, max(created) as created "
              "from jars "
              "where version not like '%-SNAPSHOT' "
              "group by group_name, jar_name) r "
              "on j.jar_name = r.jar_name "
              ;; Join with latest release
              "join "
              "(select jar_name, created, version from jars) as j2 "
              "on j2.jar_name = j.jar_name "
              "and j2.created = r.created "
              "where j.group_name = ? "
              "order by j.group_name asc, j.jar_name asc")
             [groupname]]
              :results))

(s/defschema Version
  {:version s/Str
   (s/optional-key :downloads) s/Int})

(s/defschema Artifact
  {:description s/Str
   :user s/Str
   :authors s/Str
   :version s/Str
   :recent_versions [Version]
   :group_name s/Str
   :jar_name s/Str
   :scm (s/maybe s/Str)
   :homepage (s/maybe s/Str)
   :downloads s/Int})

(s/defschema GroupsArtifact
  (-> Artifact
      (dissoc :recent_versions :version)
      (assoc :latest_version s/Str
             :latest_release s/Str)))

(s/defschema User
  {:groups [s/Str]})

(defapi api-routes
  (swagger-ui "/api-docs")
  (swagger-docs
    {:info {:title "Clojars API"}})
  (context "/api" []
    (GET* ["/groups/:group-id", :group-id #"[^/]+"] [group-id]
      :tags ["groups"]
      :return [GroupsArtifact]
      (let [stats (stats/all)]
        (-> (jars-by-groupname group-id)
            (->> (map (fn [jar]
                        (-> jar
                            (rename-keys {:version :latest_version})
                            (dissoc :id :created :promoted_at)
                            (assoc :downloads (stats/download-count stats group-id (:jar_name jar)))))))
            ok)))
    (GET* ["/artifacts/:artifact-id", :artifact-id #"[^/]+"] [artifact-id]
      :tags ["artifact"]
      :return Artifact
      (ok (get-artifact artifact-id artifact-id)))
    (GET* ["/artifacts/:group-id/:artifact-id", :group-id #"[^/]+", :artifact-id #"[^/]+"] [group-id artifact-id]
      :tags ["artifact"]
      :return Artifact
      (ok (get-artifact group-id artifact-id)))
    (GET* "/users/:username" [username]
      :tags ["users"]
      :return User
      (ok {:groups (db/find-groupnames username)}))
    (ANY* "*" _
      (not-found nil))))
