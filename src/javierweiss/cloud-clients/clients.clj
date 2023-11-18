(ns javierweiss.cloud-clients.clients
  (:require [cognitect.aws.client.api :as aws])
  (:import (com.azure.storage.blob
            BlobClientBuilder BlobServiceClientBuilder)))

(def cliente-aws-s3 (aws/client {:api :s3
                                 :region :us-east-1}))

(def cliente-azure-blob (-> (BlobClientBuilder.)
                          (.endpoint "")
                          (.blobName "luhmannblob") 
                          (.containerName "archivosociologico")
                          (.sasToken "")
                          (.buildClient)))


(comment 
  (def service (-> (BlobServiceClientBuilder.)
                   (.endpoint "") 
                   (.sasToken "")
                   (.buildClient)))
  (def cliente-container (.getBlobContainerClient service "archivosociologico"))
  (for [item (.listBlobs cliente-container)] (.getName item))
  (.downloadToFile cliente-azure-blob "Luhmann Der neue Chef.pdf")
   
  )