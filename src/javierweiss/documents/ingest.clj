(ns javierweiss.documents.ingest
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]
            [cohere.client :refer :all]
            [cognitect.aws.client.api :as aws]))

;; TODO
;; 1. ¿Usar migratus para gestionar el SQL? (No es prioridad)
;; 2. Buscar cómo recorrer el repositorio s3 y crear un pipeline que agilice todo el proceso
;; 2.1. Realizar los embeddings con la API de cohere

(py/initialize! :python-executable (str (System/getenv "CONDA_DIR") "/envs/luhmann/bin/python3.10") 
                :library-path (str (System/getenv "CONDA_DIR") "/envs/lib/libpython3.10.so"))
(require '[libpython-clj2.require :refer [require-python]])

(def s3-loader (py/from-import langchain.document_loaders S3FileLoader))

(def cliente-s3 (aws/client {:api :s3
                             :region :us-east-1}))

(defn listar-obras
  []
  (into []
        (->> (aws/invoke cliente-s3 {:op :ListObjects 
                                     :request {:Bucket "luhmann-bucket"}})
             :Contents
             (map :Key)
             rest)))

(defn cargar
  [doc]
  (py. (s3-loader "luhmann-bucket" doc) load))

(defn crear-documentos
  []
  (let [obras (listar-obras)]
    (map (fn [doc] (cargar doc)) obras)))


 



(comment
  
  doc 

  (aws/ops cliente-s3)

  (aws/invoke cliente-s3 {:op :ListBuckets})
  (aws/invoke cliente-s3 {:op :ListObjects :request {:Bucket "luhmann-bucket"}})

  (def obras (listar-obras))

  (cargar (second obras))
  
  (-> (cargar (obras 3))
      first
      (py.- page_content))
  
  (crear-documentos)

  (def doc (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann limits of steering.pdf") load))
  
  (def doc1 (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann the theory of social systems and its epistemology reply to danilo zolos critical comments.pdf") load))
  
  (py.- (first doc) page_content)
  
  (py.- (first doc1) page_content)
  
  (py.- (first doc1) metadata)
  
  
  )