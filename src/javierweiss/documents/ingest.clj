(ns javierweiss.documents.ingest
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]] 
            [cognitect.aws.client.api :as aws]
            [javierweiss.configuracion.config :refer [configuracion]]))

(def config (configuracion))

;; TODO
;; 1. ¿Usar migratus para gestionar el SQL? (No es prioridad)
;; 2. Realizar los embeddings con la API de cohere

(py/initialize! :python-executable (str (System/getenv "CONDA_DIR") "/envs/luhmann/bin/python3.10") 
                :library-path (str (System/getenv "CONDA_DIR") "/envs/lib/libpython3.10.so"))

(def s3-loader (py/from-import langchain.document_loaders S3FileLoader))

(def cliente-s3 (aws/client {:api :s3
                             :region :us-east-1}))

(defn listar-obras
  []
  (into []
        (->> (aws/invoke cliente-s3 {:op :ListObjects 
                                     :request {:Bucket (:bucket-name config)}})
             :Contents
             (map :Key)
             rest)))

(defn cargar
  [doc]
  (py. (s3-loader (:bucket-name config) doc) load))

(defn crear-documentos
  []
  (into []
        (doseq [obra (listar-obras)] (cargar obra))))

(comment

  (def dir-loader (py/from-import langchain.document_loaders S3DirectoryLoader))

  (py. (dir-loader "luhmann-bucket") load)

  doc

  (aws/ops cliente-s3)

  (-> (aws/ops cliente-s3) keys)

  (aws/invoke cliente-s3 {:op :GetObject :request {:Bucket "luhmann-bucket"
                                                   :Key "curso_doctorado/Luhmann, Niklas-The Control of Intransparency (1997).pdf"}})

  (aws/doc cliente-s3 :GetObject)

  (aws/invoke cliente-s3 {:op :ListBuckets})
  (tap> (aws/invoke cliente-s3 {:op :ListObjects :request {:Bucket "luhmann-bucket"}}))

  (def obras (listar-obras))

  (count obras)

  (tap> obras) 
  
  (cargar (obras 16))

  ;; El último texto, Trust and Power está dando problemas. Arroja excepción: FileNotFoundError: [Errno 2] No such file or directory: 'pdfinfo'
  ;; pdf2image.exceptions.PDFInfoNotInstalledError: Unable to get page count. Is poppler installed and in PATH? <= Ya se instaló
  
  (doseq [obra obras] (println (cargar obra)))


  (crear-documentos)

  (def doc (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann limits of steering.pdf") load))

  (def doc1 (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann the theory of social systems and its epistemology reply to danilo zolos critical comments.pdf") load))

  (py.- (first doc) page_content)

  (py.- (first doc1) page_content)

  (py.- (first doc1) metadata)


  )