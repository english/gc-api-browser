(ns deploy
  (:require [clojure.string :as str]
            [aws.sdk.s3 :as s3]
            [pantomime.mime :as mime]))

(defn filename->content-encoding [filename]
  (let [ext (last (str/split filename #"\."))]
    (when (#{"css" "js"} ext)
      "gzip")))

(defn filename->metadata [filename]
  {:content-type  (mime/mime-type-of filename)
   :content-encoding  (filename->content-encoding filename)})

(defn upload-file! [bucket access-key secret-key file]
  (let [filename (.getName file)
        metadata (filename->metadata filename)
        permissions (s3/grant :all-users :read)
        cred {:access-key access-key
              :secret-key secret-key
              :endpoint "s3.amazonaws.com"}]
    (future (s3/put-object cred bucket filename file metadata permissions)
            file)))

(defn upload-directory! [directory bucket access-key secret-key]
  (let [files (->> (file-seq directory)
                   (remove (memfn isHidden))
                   (filter (memfn isFile)))]
    (future
      (doseq [file (map (partial upload-file! bucket access-key secret-key) files)]
        (println "uploaded" (.getName @file))
        directory))))

(defn -main [dir bucket access-key secret-key]
  (deref (upload-directory! (clojure.java.io/file dir) bucket access-key secret-key))
  (System/exit 0))
