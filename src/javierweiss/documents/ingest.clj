(ns javierweiss.documents.ingest
  (:require [com.brunobonacci.mulog :as u]
            [javierweiss.split.splitters.langchainsplitter :as sp]
            [libpython-clj2.python :as py]))

(defn crear-documentos
  [list-or-listfn load-fn]
  (u/log ::ingesta-de-documentos :status :inicio)
  (let [obras (if (fn? list-or-listfn) (list-or-listfn) list-or-listfn)]
    (keep identity
          (for [obra obras] (load-fn obra)))))


(comment
   
  (require '[javierweiss.cloudclients.listados :refer [listar-obras]]
           '[javierweiss.load.load :refer [load-document]] 
           '[javierweiss.split.split :refer [split]]
           '[libpython-clj2.python :as py :refer [py. py.. py.-]]
           '[javierweiss.embed.embeddings.cohereembed :refer [cohere-embedding]])

  (def res (crear-documentos (take 2 (listar-obras :azure)) (partial load-document :langchain-azure-singleblob)))
 
  res 
 (def splitting (split sp/langchain-split-documents sp/token-splitter (first res)))
  
  (def chunkk (py.- (py/get-item splitting 0) page_content))
  
  chunkk
    (py/->py-list [chunkk])
  (cohere-embedding {:texts [["Hola"]] :model "embed-multilingual-v3.0" :truncate "END"})
  (cohere-embedding :texts [chunkk] :model "embed-multilingual-v3.0" :truncate "END")

  (require '[cohere.client :as client])

  (client/generate :prompt "Hey, there! What is your name?")
  (client/embed :texts [chunkk])

  (System/getProperty "cohere.api.key")
  )