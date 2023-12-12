(ns javierweiss.split.split
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]] 
            [javierweiss.utils.utils :as ut])) 

(defn split
  "Recibe una función de splitting, un splitter y un documento python"
  [split-fn splitter doc & {:keys [size overlap] :as opts}]
  (split-fn splitter doc opts))

(comment

  ;; Echar un ojo a SentenceTransformersTokenTextSplitter, NLTKTextSplitter, SpacyTextSplitter
  
  
  
  )