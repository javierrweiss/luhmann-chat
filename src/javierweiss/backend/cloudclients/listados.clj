(ns javierweiss.backend.cloudclients.listados
  (:require [cognitect.aws.client.api :as aws]
            [javierweiss.backend.cloudclients.clients :refer [cliente]]
            [javierweiss.backend.configuracion.config :refer [configuracion-storage]]
            [com.brunobonacci.mulog :as u])
  (:import java.io.IOException))

(defmulti listar-obras :seleccion)
 
(defmethod listar-obras :aws [conf]
  (let [config (:storage-service conf)]
    (into []
          (try
            (->> (aws/invoke cliente {:op :ListObjects
                                        :request {:Bucket (:bucket-name config)}})
                 :Contents
                 (map :Key)
                 rest)
            (catch IOException e (u/log ::error-listado-obras :mensaje (.getMessage e)))))))

(defmethod listar-obras :azure
  [_]
  (into [] (for [item (.listBlobs cliente)] (.getName item))))

(defmethod listar-obras :default
  [_]
  (throw (IllegalArgumentException. "La opción elegida no se encuentra implementada")))

(def obras (listar-obras configuracion-storage))
 
(comment
  (listar-obras configuracion-storage)
  (listar-obras {:storage-service {}
                 :seleccion :aws})
  (listar-obras {:storage-service {}
                 :seleccion :a})
  (ns-interns *ns*)
  (ns-unmap *ns* 'listar-obras)
  (remove-all-methods listar-obras)

  )
