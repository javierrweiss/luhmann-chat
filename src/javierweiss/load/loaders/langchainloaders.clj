(ns javierweiss.load.loaders.langchainloaders
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]))

(def azure-blob (py/from-import langchain.document_loaders AzureBlobStorageContainerLoader))

(def aws-s3 (py/from-import langchain.document_loaders S3FileLoader))