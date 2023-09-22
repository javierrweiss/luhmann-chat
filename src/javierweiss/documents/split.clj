(ns javierweiss.documents.split
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]
            [javierweiss.documents.ingest :as ing :refer [crear-documentos]]
            [javierweiss.utils.utils :as ut])) 

(def splitter (py/from-import langchain.text_splitter CharacterTextSplitter))

(def token-splitter (py/from-import langchain.text_splitter TokenTextSplitter))

(defn split
  "Recibe un objeto python del tipo Splitter (e.g. CharacterTextSplitter o TokenTextSplitter), el documento, un booleano indicando si recibe una lista 
   de documentos (true) o una lista de textos (false) y opcionalmente el tamaño y la sobreposición. Devuelve documentos divididos en chunks más pequeños."
  [splitter doc single-doc? & {:keys [size overlap] :or {size 750 overlap 10}}]
  (if single-doc? 
    (py. (splitter :chunk_size size :chunk_overlap overlap) split_documents doc)
    (py. (splitter :chunk_size size :chunk_overlap overlap) create_documents doc)))

(defn split-all-docs
  []
  (let [docs (crear-documentos)]
    (split token-splitter docs true)))


(comment

  ;; Echar un ojo a SentenceTransformersTokenTextSplitter, NLTKTextSplitter, SpacyTextSplitter
  
  ;; Parece que hay conflictos entre Portal y python-clj
  
  (def obras (ing/listar-obras))
  (tap> obras)
  (let [doc (ing/cargar (obras 3))
        page (py.- (first doc) page_content)]
    (py. (token-splitter :chunk_size 750 :chunk_overlap 10) create_documents page))

  ;; CharacterTextSplitter
  (def sp (split splitter (ing/cargar (obras 3)) true))
  (count sp)
  (tap> (second sp))

  ;;TokenTextSplitter 
  (def sp2 (split token-splitter (ing/cargar (obras 3)) true))
  (count sp2)
  (second sp2)
  (type (second sp2)) 
  (py.- (second sp2) page_content) 
  (py/get-attr (second sp2) "page_content")
  (keys (py/get-attr (second sp2) "__dict__"))
  (get (py/get-attr (second sp2) "__dict__") "metadata")
  (type (into {} (py/get-attr (second sp2) "__dict__")))
  (nth sp2 4)
  (tap> (py.- (nth sp2 10) "page_content"))
  (ut/py-obj->clj-map (second sp2))


  ;; Procesar todo
  
  (def all (mapv (fn [doc] (split token-splitter doc true)) (crear-documentos)))

  all
  
  (def all-docs (split-all-docs)) 
  (tap> (count all-docs)) 
  )