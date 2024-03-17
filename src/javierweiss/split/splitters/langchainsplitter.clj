(ns javierweiss.split.splitters.langchainsplitter
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]))

(def splitter (py/from-import langchain.text_splitter CharacterTextSplitter))

(def token-splitter (py/from-import langchain.text_splitter TokenTextSplitter))

(defn langchain-split-documents
  "Recibe una lista de objetos del tipo Documento y devuelve otra lista de objetos de Documentos"
  [splitter doc & {:keys [size overlap] :or {size 750 overlap 10}}] 
  (py. (splitter :chunk_size size :chunk_overlap overlap) split_documents doc))

(defn langchain-create-documents
  "Recibe un string y devuelve una lista de tipo documentos"
  [splitter doc & {:keys [size overlap] :or {size 750 overlap 10}}]
  (py. (splitter :chunk_size size :chunk_overlap overlap) create_documents doc))

(defn langchain-split-text
  "Recibe un string y devuelve un vector de strings"
  [splitter doc & {:keys [size overlap] :or {size 750 overlap 10}}]
  (py. (splitter :chunk_size size :chunk_overlap overlap) split_text doc))


(comment 

  (py/get-attr (ffirst javierweiss.documents.ingest/res) "page_content")

  (-> (first javierweiss.documents.ingest/res)
      (py/get-item 0)
      (py/get-attr "page_content")
      count) 
  
  (py. (token-splitter :chunk_size 750 :chunk_overlap 10) create_documents  (-> (first javierweiss.documents.ingest/res)
                                                                                (py/get-item 0)
                                                                                (py/get-attr "page_content")))
  
  (count (py. (token-splitter :chunk_size 750 :chunk_overlap 10) split_text  (-> (first javierweiss.documents.ingest/res)
                                                                                 (py/get-item 0) 
                                                                                 (py/get-attr "page_content"))))

  (def tkn (py. (token-splitter :chunk_size 750 :chunk_overlap 10) split_documents (first javierweiss.documents.ingest/res)))
  
  (-> tkn
      #_(py/call-attr "__len__")
      (py/get-item 0)
      (py/get-attr "page_content")
      count)
   (mapv #(py/get-attr % :page_content) tkn)

  )