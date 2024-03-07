(ns javierweiss.cloudclients.uploads
  (:require [javierweiss.cloudclients.clients :refer [cliente]]
            [cognitect.aws.client.api :as aws]
            [javierweiss.configuracion.config :refer [configuracion-storage]]
            [clojure.java.io :as io]
            [com.brunobonacci.mulog :as u])
  (:import java.io.IOException))

(defmulti almacenar (fn [conf _ _ _] 
                      (:seleccion conf)))

(defmethod almacenar :aws [conf ruta nombre carpeta]
  (let [config (:storage-service conf)]
    (try
      (aws/invoke cliente {:op :PutObject :request {:Bucket (str (:bucket-name config) carpeta)
                                                    :Key nombre
                                                    :Body (io/input-stream ruta)}})
      (catch IOException e (u/log ::error-almacenamiento-s3 :mensaje (.getMessage e))))))

(defmethod almacenar :default [_ _ _ _]
  (throw (IllegalArgumentException. "La opción seleccionada no está implementada")))

(def almacena (partial almacenar configuracion-storage))

(comment 
 
  (almacenar {:seleccion :aws :storage-service {}} "/ruta/larga" "archivoX.pdf" "/carpetaPeta")
  (almacenar {:seleccion :azure :storage-service {}} "/ruta/larga" "archivoX.pdf" "/carpetaPeta")
  (ns-unmap *ns* 'almacenar) 
  )


