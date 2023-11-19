(ns javierweiss.cloudclients.listados
  (:require [cognitect.aws.client.api :as aws]
            [javierweiss.cloudclients.clients :as c :refer [providers]]
            [javierweiss.configuracion.config :refer [configuracion]]
            [com.brunobonacci.mulog :as u])
  (:import java.io.IOException))

(def config (configuracion))

(defmulti listar-obras (fn [p] (providers p)))
 
(defmethod listar-obras :aws [_]
  (into []
        (try
          (->> (aws/invoke c/cliente-aws-s3 {:op :ListObjects
                                             :request {:Bucket (:bucket-name config)}})
               :Contents
               (map :Key)
               rest)
          (catch IOException e (u/log ::error-listado-obras :mensaje (.getMessage e))))))

(defmethod listar-obras :azure
  [_]
  (into [] (for [item (.listBlobs c/cliente-azure-container-blob)] (.getName item))))
 
(comment
  (listar-obras :azure)
  (listar-obras )
  (ns-interns *ns*)
  (ns-unmap *ns* 'listar-obras)
  (remove-all-methods listar-obras)

  )
