(ns javierweiss.cloudclients.listados
  (:require [cognitect.aws.client.api :as aws]
            [javierweiss.cloudclients.clients :as c]
            [javierweiss.configuracion.config :refer [configuracion]]
            [com.brunobonacci.mulog :as u])
  (:import java.io.IOException))

(def config (configuracion))

(defmulti listar-obras (fn [p] ((descendants :javierweiss.cloudclients.clients/cloud-provider) p)))
 
(defmethod listar-obras :javierweiss.cloudclients.clients/aws [_]
  (into []
        (try
          (->> (aws/invoke c/cliente-aws-s3 {:op :ListObjects
                                             :request {:Bucket (:bucket-name config)}})
               :Contents
               (map :Key)
               rest)
          (catch IOException e (u/log ::error-listado-obras :mensaje (.getMessage e))))))

(defmethod listar-obras :javierweiss.cloudclients.clients/azure
 []
  )

(comment
  (listar-obras :javierweiss.cloudclients.clients/aws) 
  (ns-interns *ns*)
  (ns-unmap *ns* 'listar-obras)

  )
