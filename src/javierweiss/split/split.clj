(ns javierweiss.split.split
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]
            [javierweiss.documents.ingest :as ing :refer [crear-documentos]]
            [javierweiss.utils.utils :as ut])) 

(defn split
  [split-fn splitter doc & {:keys [size overlap] :as opts}]
  (split-fn splitter doc opts))

(comment

  ;; Echar un ojo a SentenceTransformersTokenTextSplitter, NLTKTextSplitter, SpacyTextSplitter
  
  
  
  )