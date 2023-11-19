(ns javierweiss.cloudclients.clients
  (:require [cognitect.aws.client.api :as aws]
            [javierweiss.configuracion.config :refer [configuracion]])
  (:import (com.azure.storage.blob
            BlobClientBuilder BlobServiceClientBuilder)))

(def config (-> (configuracion) :storage-service :azure))

(def cliente-aws-s3 (aws/client {:api :s3
                                 :region :us-east-1}))

#_(def cliente-azure-blob (-> (BlobClientBuilder.)
                          (.endpoint "")
                          (.blobName "luhmannblob") 
                          (.containerName "archivosociologico")
                          (.sasToken "")
                          (.buildClient)))

(def providers [::aws ::azure])

(map #(derive % ::cloud-provider) providers)

(comment
  (def cliente-azure-blob (-> (BlobServiceClientBuilder.)
                              (.connectionString (:blob_conn_string config))
                              (.buildClient)
                              (.getBlobContainerClient (:container config))
                              (.getBlobClient (:blobname config))))
   
  (.downloadToFile cliente-azure-blob "Luhmann Der neue Chef.pdf")
   

  (def cliente-container (.getBlobContainerClient service "archivosociologico"))
  (for [item (.listBlobs cliente-container)] (.getName item))
  (.downloadToFile cliente-azure-blob "Luhmann Der neue Chef.pdf")

  config
  )