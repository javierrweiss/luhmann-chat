(ns javierweiss.load.load
  (:require [javierweiss.load.loaders.langchainloaders :as lngld]
            [javierweiss.configuracion.config :refer [configuracion]]
            [libpython-clj2.python :as py :refer [py. py.-]]))

(def azure-config (-> (configuracion) :storage-service :azure))

(def aws-config (-> (configuracion) :storage-service :aws))
 
(def loading-services #{:langchain-aws :langchain-azure-singleblob :langchain-azure-blob :llama})

(defmulti load-document (fn [service _] (loading-services service)))

(defmethod load-document :langchain-azure-blob 
  [_ _]
  (py. (lngld/AzureBlobStorageContainerLoader :conn_str (:blob_conn_string azure-config) :container (:container azure-config)) load))

(defmethod load-document :langchain-azure-singleblob
 [_ file]
 (py. (lngld/AzureBlobStorageFileLoader :conn_str (:blob_conn_string azure-config) :container (:container azure-config) :blob_name file) load))

(defmethod load-document :langchain-aws
 [_ file] 
 (py. (lngld/S3FileLoader (:bucket-name aws-config) file) load))


(comment
    
  ;; Toma una eternidad y falla con archivos .djvu
   (def docs 
     (py. (lngld/AzureBlobStorageContainerLoader :conn_str (:blob_conn_string azure-config) :container (:container azure-config)) load))

  (def doc (load-document :langchain-azure-singleblob "Luhmann Der neue Chef.pdf"))
  (py.- (py/get-item doc  0) page_content) 
  (remove-all-methods load-document) 
  (ns-unmap *ns* 'load-document)
  )