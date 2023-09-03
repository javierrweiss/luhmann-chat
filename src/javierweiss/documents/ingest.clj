(ns javierweiss.documents.ingest
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]
            [cohere.client :refer :all]))
 
;; TODO
;; 1. Cargar las variables de entorno para cohere y la base de datos
;; 2. Crear el archivo de sql para crear la tabla (usar migratus?)
;; 3. Crear el archivo en sql para guardar los embeddings en el base
;; 4. Buscar cómo recorrer el repositorio s3 y crear un pipeline que agilice todo el proceso
;; 4.1. Realizar los embeddings con la API de cohere

(py/initialize! :python-executable (str (System/getenv "CONDA_DIR") "/envs/luhmann/bin/python3.10") 
                :library-path (str (System/getenv "CONDA_DIR") "/envs/lib/libpython3.10.so"))
 
(require '[libpython-clj2.require :refer [require-python]])

(def s3-loader (py/from-import langchain.document_loaders S3FileLoader))

(def splitter (py/from-import langchain.text_splitter CharacterTextSplitter))

(def token-splitter (py/from-import langchain.text_splitter TokenTextSplitter))
 
(def doc (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann limits of steering.pdf") load))

(def doc1 (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann the theory of social systems and its epistemology reply to danilo zolos critical comments.pdf") load))
 
(py.- (first doc) page_content)

(py.- (first doc1) page_content)

(py.- (first doc1) metadata)

(def sp-doc1 (py. (token-splitter :chunk_size 750 :chunk_overlap 10) split_documents doc1))

(count sp-doc1)

(first sp-doc1)

(second sp-doc1)
