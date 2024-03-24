(ns javierweiss.backend.cloudclients.clients
  (:require [cognitect.aws.client.api :as aws]
            [javierweiss.backend.configuracion.config :refer [configuracion-storage]])
  (:import (com.azure.storage.blob BlobServiceClientBuilder)))

(defmulti obtener-cliente :seleccion)

(defmethod obtener-cliente :aws [_]
  (aws/client {:api :s3
               :region :us-east-1}))

(defmethod obtener-cliente :azure [conf]
  (let [config (:storage-service conf)]
    (-> (BlobServiceClientBuilder.)
        (.connectionString (:blob_conn_string config))
        (.buildClient)
        (.getBlobContainerClient (:container config)))))

(defmethod obtener-cliente :default [_]
  (throw (IllegalArgumentException. "Opción no implementada")))


(def cliente (obtener-cliente configuracion-storage))

(comment

  (obtener-cliente configuracion-storage)

  (obtener-cliente {:seleccion :aws
                    :storage-service {}})
  
  (obtener-cliente {:seleccion :aks
                    :storage-service {}})
  
  (def cliente-azure-blobclient (.getBlobClient cliente-azure-container-blob (:blobname config)))
  
  (defn listar-azure
    []
    (for [item (.listBlobs cliente-azure-container-blob)] (.getName item)))
  
  (def lista (listar-azure))
  
  (.downloadToFile cliente-azure-blobclient (str "resources/" (first lista)))

  (ns-unmap *ns* 'obtener-cliente)
  )