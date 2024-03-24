(ns javierweiss.backend.split.split
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]] 
            [javierweiss.backend.split.splitters.langchainsplitter :refer [langchain-create-documents langchain-split-documents splitter token-splitter]])) 

(defn create-splitting-fn
  [split-fn splitter]
  (partial split-fn splitter))

(def split-by-character "doc & {:keys [size overlap] :as opts}" (create-splitting-fn langchain-create-documents splitter))

(def split-by-token "doc & {:keys [size overlap] :as opts}" (create-splitting-fn langchain-split-documents token-splitter))

(comment

  ;; Echar un ojo a SentenceTransformersTokenTextSplitter, NLTKTextSplitter, SpacyTextSplitter
 (mapv (fn [document] (py/get-attr document :page_content)) (split-by-token (first javierweiss.documents.ingest/res)))
  
  
  )