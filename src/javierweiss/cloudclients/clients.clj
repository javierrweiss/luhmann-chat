(ns javierweiss.cloudclients.clients
  (:require [cognitect.aws.client.api :as aws]
            [javierweiss.configuracion.config :refer [configuracion]])
  (:import (com.azure.storage.blob BlobServiceClientBuilder)))

(def config (-> (configuracion) :storage-service :azure))

(def cliente-aws-s3 (aws/client {:api :s3
                                 :region :us-east-1}))

(def cliente-azure-container-blob (-> (BlobServiceClientBuilder.)
                                      (.connectionString (:blob_conn_string config))
                                      (.buildClient)
                                      (.getBlobContainerClient (:container config))))

(def cliente-azure-blobclient (.getBlobClient cliente-azure-container-blob (:blobname config)))

(def providers (->> (configuracion) :storage-service keys (apply hash-set)))

(comment


  cliente-azure-blobclient

  

  (defn listar-azure
    []
    (for [item (.listBlobs cliente-azure-container-blob)] (.getName item)))
  
  (def lista (listar-azure))
  
  (.downloadToFile cliente-azure-blobclient (str "resources/" (first lista)))

  config
  )