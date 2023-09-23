(ns javierweiss.documents.ingest
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]] 
            [cognitect.aws.client.api :as aws]
            [javierweiss.configuracion.config :refer [configuracion]]
            [clojure.java.io :as io]
            [com.brunobonacci.mulog :as u])
  (:import java.io.IOException))

(def config (configuracion))

(py/initialize! :python-executable (str (System/getenv "CONDA_DIR") "/envs/luhmann/bin/python3.10") 
                :library-path (str (System/getenv "CONDA_DIR") "/envs/lib/libpython3.10.so"))

(def s3-loader (py/from-import langchain.document_loaders S3FileLoader))

(def cliente-s3 (aws/client {:api :s3
                             :region :us-east-1}))

(defn listar-obras
  []
  (into []
        (try
          (->> (aws/invoke cliente-s3 {:op :ListObjects 
                                       :request {:Bucket (:bucket-name config)}})
               :Contents
               (map :Key)
               rest)
          (catch IOException e (u/log ::error-listado-obras :mensaje (.getMessage e))))))

(defn cargar
  [doc]
  (try
    (u/log ::carga-documentos :status (str "Cargando " doc "..."))
    (py. (s3-loader (:bucket-name config) doc) load)
    (catch Exception e (u/log ::error-carga-documentos :mensaje (.getMessage e)))))

(defn crear-documentos
  []
  (when-let [obras (listar-obras)]
    (keep identity
          (for [obra obras] (cargar obra)))))

(defn almacenar-archivo
  "Recibe String indicando la ruta, el nombre correponde a la llave con que se va a identificar en S3 y un string indicando la carpera 
   (/carpeta_X) -puede ser nil"
  [ruta nombre carpeta]
  (try 
    (aws/invoke cliente-s3 {:op :PutObject :request {:Bucket (str (:bucket-name config) carpeta) 
                                                        :Key nombre
                                                        :Body (io/input-stream ruta)}})
    (catch IOException e (u/log ::error-almacenamiento-s3 (.getMessage e)))))

(comment

  (almacenar-archivo "/workspaces/luhmann-chat/resources/Niklas Luhmann - Macht-Lucius & Lucius (2003).pdf" "Niklas Luhmann Macht" nil)

  (almacenar-archivo "/workspaces/luhmann-chat/resources/Niklas Luhmann - Organisation und Entscheidung.-Westdeutscher Verlag (2000).pdf"
                     "Niklas Luhmann Organisation und Entscheidung"
                     "/curso_doctorado")

  (def dir-loader (py/from-import langchain.document_loaders S3DirectoryLoader))

  (py. (dir-loader "luhmann-bucket") load)

  doc

  (aws/ops cliente-s3) 

  (tap> (-> (aws/ops cliente-s3) keys))

  (tap> (filter (fn [k] (re-seq #"Put\w+|Upload\w+" (str k))) (-> (aws/ops cliente-s3) keys)))
  
  (aws/doc cliente-s3 :PutObject)

  (aws/doc cliente-s3 :Objects)

  (aws/invoke cliente-s3 {:op :GetObject :request {:Bucket "luhmann-bucket"
                                                   :Key "curso_doctorado/Luhmann, Niklas-The Control of Intransparency (1997).pdf"}})

  (aws/doc cliente-s3 :GetObject)

  (aws/invoke cliente-s3 {:op :ListBuckets})
  
  (tap> (aws/invoke cliente-s3 {:op :ListObjects :request {:Bucket "luhmann-bucket"}}))

  (def obras (listar-obras))

  (count obras)

  (tap> obras) 
   
  (cargar (obras 20))

  ;; El último texto, Trust and Power está dando problemas. Arroja excepción: FileNotFoundError: [Errno 2] No such file or directory: 'pdfinfo'
  ;; pdf2image.exceptions.PDFInfoNotInstalledError: Unable to get page count. Is poppler installed and in PATH? <= Ya se instaló
  
  (doseq [obra obras] (println (cargar obra)))

  (def docs (crear-documentos))
  docs 
  (count docs)
  (ffirst docs)
 (require '[javierweiss.utils.utils :refer [py-obj->clj-map]])
   
  (py.- (py/get-item (first docs) 0) page_content)

  (def doc (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann limits of steering.pdf") load))

  (def doc1 (py. (s3-loader "luhmann-bucket" "curso_doctorado/Luhmann the theory of social systems and its epistemology reply to danilo zolos critical comments.pdf") load))

  (py.- (first doc) page_content)

  (py.- (first doc1) page_content)

  (py.- (first doc1) metadata)


  )