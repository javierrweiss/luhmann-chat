(ns javierweiss.documents.split
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]
            [javierweiss.documents.ingest :as ing :refer [crear-documentos]]))

(def splitter (py/from-import langchain.text_splitter CharacterTextSplitter))

(def token-splitter (py/from-import langchain.text_splitter TokenTextSplitter))

(defn split
  "Recibe un objeto python del tipo Splitter (e.g. CharacterTextSplitter o TokenTextSplitter), el documento y opcionalmente el tamaño y la sobreposición.
   Devuelve documentos divididos en chunks más pequeños."
  [splitter doc & {:keys [size overlap] :or {size 750 overlap 10}}]
  (py. (splitter :chunk_size size :chunk_overlap overlap) split_documents doc))

(comment
  (def obras (ing/listar-obras))  
  (tap> obras)
  (let [doc (ing/cargar (obras 3))  
        page (py.- (first doc) page_content)]
      (py. (token-splitter :chunk_size 750 :chunk_overlap 10) create_documents page))
  
  (def sp (split splitter (ing/cargar (obras 3))))
  (count sp)
  (tap> (second sp))

  (def sp-doc1 (py. (token-splitter :chunk_size 750 :chunk_overlap 10) split_documents doc))
  (count sp-doc1)
  (first sp-doc1) 
  (count sp-doc1) 
  (first sp-doc1) 
  (second sp-doc1)

  (def sp2 (split token-splitter (ing/cargar (obras 4))))
  (count sp2)
  (second sp2)
  (nth sp2 4)
  (tap> (nth sp2 10))
   
  )