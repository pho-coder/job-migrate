(ns job-migrate.core
  (:gen-class)
  (:require [clj-zookeeper.zookeeper :as zk]
            [com.jd.bdp.magpie.util.utils :as m-utils]
            [clojure.tools.cli :refer [parse-opts]]
            [clojure.tools.logging :as log]))

(def cli-options
  [["-j" "--job-list-file file" "job list file"
    :default "jobs"]
   ["-z" "--zk zookeeper" "zookeeper address"
    :default "d"]
   ["-h" "--help"]])

(defn usage [options-summary]
  (->> ["This is my program. There are many like it, but this one is mine."
        ""
        "Usage: program-name [options] action"
        ""
        "Options:"
        options-summary
        ""
        "Actions:"
        "  migrate-jobs  get kill and submit scribes"
        "  query-jobs    query jobs"
        ""
        "Please refer to the manual page for more information."]
       (clojure.string/join \newline)))

(defn error-msg [errors]
  (str "The following errors occurred while parsing your command:\n\n"
       (clojure.string/join \newline errors)))

(defn exit [status msg]
  (println msg)
  (System/exit status))

(defn migrate-jobs [job-list zk-client]
  (log/info (str "total jobs:" (.size job-list)))
  (let [path (str (m-utils/timestamp2datetime (System/currentTimeMillis) "yyyy_MM_dd_HH_mm_ss"))]
    (clojure.java.io/make-parents (str path "/tasks/a"))
    (spit (str path "/kill-cmds") "#!/usr/bin/env bash\nset -e\n")
    (spit (str path "/submit-cmds") "#!/usr/bin/env bash\nset -e\n")
    (doseq [task-id job-list]
      (log/info task-id)
      (try
        (let [task-info-bytes (zk/get-data zk-client (str "/magpie/assignments/" task-id))]
          (if (nil? task-info-bytes)
            (do (log/error task-id "nil!")
                (exit 1 (error-msg task-id "nil!")))
            (let [task-info (m-utils/bytes->map task-info-bytes)]
              (spit (str path "/tasks/" task-id) (str task-info "\n"))
              (spit (str path "/kill-cmds") (str "magpie-client kill -d -id " (get task-info "id") "\n") :append true)
              (spit (str path "/submit-cmds") (str "magpie-client submit -class " (get task-info "class") " -id " (get task-info "id") " -jar " (get task-info "jar") " -group " (get task-info "group") " -type " (get task-info "type") " -d\n") :append true))))
        (catch Exception e
          (log/error e)
          (throw e))))))

(defn query-jobs [job-list zk-client]
  (log/info (str "total jobs:" (.size job-list))))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (println "Hello, World!")
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    ;; Handle help and error conditions
    (log/info options)
    (log/info arguments)
    (cond
      (:help options) (exit 0 (usage summary))
      (not= (count arguments) 1) (exit 2 (usage summary))
      errors (exit 3 (error-msg errors)))
    
    (let [job-list-file (:job-list-file options)
          zk-address (:zookeeper options)
          zk-client (zk/new-client zk-address)
          job-list (clojure.string/split (slurp job-list-file) #"\n")]
      (log/info (str "job list file: " job-list-file))
      (log/info (str "zk address: " zk-address))
      (case (first arguments)
        "migrate-jobs" (migrate-jobs job-list zk-client)
        "query-jobs" (query-jobs job-list zk-client)
        (exit 4 (usage summary))))))
