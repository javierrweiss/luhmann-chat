(ns javierweiss.cloudclients.uploads
  (:require [javierweiss.cloudclients.clients :as c]
            [cognitect.aws.client.api :as aws]
            [javierweiss.configuracion.config :refer [configuracion]]
            [clojure.java.io :as io]
            [com.brunobonacci.mulog :as u])
  (:import java.io.IOException))

(def config (configuracion))

(defmulti almacenar (fn [p _ _ _] ((descendants :javierweiss.cloudclients.clients/cloud-provider) p)))

(defmethod almacenar :javierweiss.cloudclients.clients/aws [_ ruta nombre carpeta]
  (try
    (aws/invoke c/cliente-aws-s3 {:op :PutObject :request {:Bucket (str (:bucket-name config) carpeta)
                                                           :Key nombre
                                                           :Body (io/input-stream ruta)}})
    (catch IOException e (u/log ::error-almacenamiento-s3 (.getMessage e)))))

(defmethod almacenar :javierweiss.cloudclients.clients/azure [_ ])

(comment 

  (almacenar :javierweiss.cloudclients.clients/aws "/ruta/larga" "archivoX.pdf" "/carpetaPeta")
  (ns-unmap *ns* 'almacenar) 
  )


