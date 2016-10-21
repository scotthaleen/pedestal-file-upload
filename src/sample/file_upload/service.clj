(ns sample.file-upload.service
  (:require
   [clojure.java.io :as io]
   [io.pedestal.http :as http]
   [io.pedestal.http.route :as route]
   ;;[io.pedestal.http.body-params :as body-params]
   [io.pedestal.http.ring-middlewares :as ring-mw]
   [io.pedestal.http.route.definition :refer [defroutes]]))


(def FILE_FORM_PARAM "image")
(def OUTPUT_DIR "/tmp/")

(defn ok
  [request]
  {:status 200 :body "ok"})


;;;
;; example of :params on request
;;

;; :params {"image" {:filename "clojure_logo.png",
;;                   :content-type "application/octet-stream",
;;                   :tempfile #object[java.io.File 0x5a13723f "/var/folders/mc/b8d36rds5x3696gyq04sgf6wjjd1mc/T/ring-multipart-6079485883736612817.tmp"],
;;                   :size 8186}}

(defn stream->bytes [is]
  (loop [b (.read is) accum []]
    (if (< b 0)
      accum
      (recur (.read is) (conj accum b)))))


(defn upload
  [request]
  (let [[in file-name] ((juxt :tempfile :filename)
                        (-> request :params (get FILE_FORM_PARAM)))
        file-bytes (with-open [is (io/input-stream in)]
                     (stream->bytes is))]
    ;; do something with file
    ;;(io/copy in (io/file OUTPUT_DIR file-name))
    
    {:status 200
     :body (prn-str file-bytes)}))


(defroutes routes
  [[["/" {:get ok}
     ["/upload" ^:interceptors  [(ring-mw/multipart-params)] {:post upload}]]]])

(def service {:env                 :prod
              ::http/routes        routes
              ::http/resource-path "/public"
              ::http/type          :jetty
              ::http/port          8080})
