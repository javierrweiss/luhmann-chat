(ns javierweiss.documents.ingest
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]))
 
(py/initialize! :python-executable (str (System/getenv "CONDA_DIR") "/envs/luhmann/bin/python3.10") 
                :library-path (str (System/getenv "CONDA_DIR") "/envs/lib/libpython3.10.so"))
 
(require '[libpython-clj2.require :refer [require-python]])

(def s3-loader (py/from-import langchain.document_loaders S3FileLoader))

(def splitter (py/from-import langchain.text_splitter CharacterTextSplitter))
 
(def doc (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann limits of steering.pdf") load))

(def doc1 (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann the theory of social systems and its epistemology reply to danilo zolos critical comments.pdf") load))
 
(py.- (first doc) page_content)

(py.- (first doc1) page_content)

(py.- (first doc1) metadata)

