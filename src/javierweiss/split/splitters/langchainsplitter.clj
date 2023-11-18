(ns javierweiss.split.splitters.langchainsplitter
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]))

(def splitter (py/from-import langchain.text_splitter CharacterTextSplitter))

(def token-splitter (py/from-import langchain.text_splitter TokenTextSplitter))

(defn langchain-split-documents 
  [splitter doc & {:keys [size overlap] :or {size 750 overlap 10}}] 
  (py. (splitter :chunk_size size :chunk_overlap overlap) split_documents doc))

(defn langchain-create-documents
  [splitter doc single-doc? & {:keys [size overlap] :or {size 750 overlap 10}}]
  (py. (splitter :chunk_size size :chunk_overlap overlap) create_documents doc))