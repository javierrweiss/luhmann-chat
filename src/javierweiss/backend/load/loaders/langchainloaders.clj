(ns javierweiss.backend.load.loaders.langchainloaders
  (:require [libpython-clj2.python :as py]))

(def azure-blob (py/from-import langchain.document_loaders AzureBlobStorageContainerLoader))

(def azure-single-blob (py/from-import langchain.document_loaders AzureBlobStorageFileLoader))

(def aws-s3 (py/from-import langchain.document_loaders S3FileLoader))