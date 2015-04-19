(ns clojars.routes.api
  (:require [compojure.core :refer [GET ANY defroutes context]]
            [compojure.route :refer [not-found]]
            [clojars.db :as db]
            [clojars.web.common :as common]
            [clojars.stats :as stats]
            [cheshire.core :as json]))

(defroutes routes
  (context "/api" []
    (GET ["/:group-id", :group-id #"[^/]+"] [group-id]
      (let [stats (stats/all)]
        (-> (db/jars-by-groupname group-id)
            (->> (map (fn [jar]
                        (assoc jar :downloads (stats/download-count stats group-id (:jar_name jar))))))
            (json/generate-string))))
    (GET ["/:group-id/:artifact-id", :group-id #"[^/]+", :artifact-id #"[^/]+"]
      [group-id artifact-id]
      (let [stats (stats/all)]
        (some-> (db/find-jar group-id artifact-id)
                (assoc :recent_versions (db/recent-versions group-id artifact-id)
                       :downloads (stats/download-count stats group-id artifact-id))
                (update-in [:recent_versions] (fn [versions]
                                                (map (fn [version]
                                                       (assoc version :downloads (stats/download-count stats group-id artifact-id (:version version))))
                                                     versions)))
                (json/generate-string))))
    (ANY "*" _
      (not-found nil))))
