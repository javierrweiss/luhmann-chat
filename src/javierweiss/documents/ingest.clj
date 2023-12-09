(ns javierweiss.documents.ingest
  (:require [com.brunobonacci.mulog :as u]
            [javierweiss.split.splitters.langchainsplitter :as sp]
            [libpython-clj2.python :as py]
            [javierweiss.split.split :refer [split]]
            [javierweiss.embed.embed :as embed]))

(defn crear-documentos
  [list-or-listfn load-fn]
  (u/log ::ingesta-de-documentos :status :inicio)
  (let [obras (if (fn? list-or-listfn) (list-or-listfn) list-or-listfn)]
    (keep identity
          (for [obra obras] (load-fn obra)))))

(defn dividir-documentos
  [coleccion-documentos]
  (->> coleccion-documentos
       (map #(split sp/langchain-split-documents sp/token-splitter %))
       (map #(map (fn [document] (py/get-attr document :page_content)) %))
       (apply concat)
       vec))

(defn crear-embeddings
  [servicio documentos-divididos]
  (embed/embed-chunk servicio {:texts documentos-divididos
                               :truncate "END"}))
 

(comment
  
  (require '[javierweiss.cloudclients.listados :refer [listar-obras]]
           '[javierweiss.load.load :refer [load-document]]
           '[libpython-clj2.python :as py :refer [py. py.. py.-]]
           '[javierweiss.embed.embed :as embed])

  (def res (crear-documentos (take 2 (listar-obras :azure)) (partial load-document :langchain-azure-singleblob)))
   res 
  (def splitting (split sp/langchain-split-documents sp/token-splitter (first res))) 
  (count splitting)
  (type splitting)
  splitting
  (count (map #(py/get-attr % :page_content) splitting)) 
  (def chunkk (py.- (py/get-item splitting 0) page_content)) 
   
  (py/->py-list [chunkk]) 
  (embed/embed-chunk :cohere {:texts [["Hola"]] :model "embed-multilingual-v3.0" :truncate "END"}) ;; Arroja error 400
  (embed/embed-chunk :cohere {:texts [chunkk] :model "embed-multilingual-v3.0" :truncate "END"}) ;; Arroja error 400
 (embed/embed-chunk :cohere {:texts [chunkk]  :truncate "END"})
  
  (require '[cohere.client :as client])
  (client/generate :prompt "Hey, there! What is your name?")
  (client/embed :texts [chunkk])

  (System/getProperty "cohere.api.key")
 
  (->>      (dividir-documentos res)  
           (crear-embeddings :cohere))
  ) 