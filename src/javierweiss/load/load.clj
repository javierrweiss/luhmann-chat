(ns javierweiss.load.load
  (:require [javierweiss.load.loaders.langchainloaders :as lngld]
            [javierweiss.configuracion.config :refer [configuracion-storage]]
            [libpython-clj2.python :as py :refer [py. py.-]]))
 
(def loading-services #{:langchain-aws :langchain-azure-singleblob :langchain-azure-blob :llama})

(defmulti load-document (fn [service _ _] (loading-services service)))

(defmethod load-document :langchain-azure-blob 
  [_ {:keys [blob_conn_string container]} _]
  (py. (lngld/AzureBlobStorageContainerLoader :conn_str blob_conn_string  :container container) load))

(defmethod load-document :langchain-azure-singleblob
 [_ {:keys [blob_conn_string container]} file]
 (py. (lngld/AzureBlobStorageFileLoader :conn_str blob_conn_string :container container :blob_name file) load))

(defmethod load-document :langchain-aws
 [_ {:keys [bucket-name]} file] 
 (py. (lngld/S3FileLoader bucket-name file) load))

(defmethod load-document :default
  [_ _ _]
  (throw (IllegalArgumentException. "El cargador elegido no se encuentra implementado.")))

(def load-all-from-storage (load-document :langchain-azure-blob configuracion-storage nil))

(def load-document-from-storage "Recibe el path de un archivo para cargar" (partial load-document :langchain-azure-singleblob configuracion-storage))


(comment
    
  ;; Toma una eternidad y falla con archivos .djvu
   (def docs 
     (py. (lngld/AzureBlobStorageContainerLoader :conn_str (:blob_conn_string azure-config) :container (:container azure-config)) load))

  (def doc (load-document :langchain-azure-singleblob "Luhmann Der neue Chef.pdf"))
  (py.- (py/get-item doc  0) page_content) 
  (remove-all-methods load-document) 
  (ns-unmap *ns* 'load-document)
  )