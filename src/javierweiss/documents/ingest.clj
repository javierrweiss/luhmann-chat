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

  ;; Load document devuelve un objeto de tipo lista por cada documento
  (def res (crear-documentos (take 2 (listar-obras :azure)) (partial load-document :langchain-azure-singleblob)))
  (count res)
  (py/get-attr (ffirst res) "page_content")
  (count (ffirst res))
  (def contenidos (map (fn [pyobj]
                         (let [elem (first pyobj)]
                           (py/get-attr elem :page_content)))
                       res))
  (println contenidos)
  (count (first contenidos))

  (def splitting (split sp/langchain-split-documents sp/token-splitter (first res)))
  (count splitting)
  (count (py/get-attr (first splitting) :page_content))
  (def extension-splits (map (fn [doc] (let [pagina (py/get-attr doc :page_content)]
                                         (count pagina)))
                             splitting))
  extension-splits
  (type splitting)
  splitting
  (count (map #(py/get-attr % :page_content) splitting))
  (def chunkk (py.- (py/get-item splitting 0) page_content))
  (println chunkk)
;; https://docs.cohere.com/reference/embed
  ;; No olvidar que el máximo de textos por llamado es 96.
  (py/->py-list [chunkk])
  (embed/embed-chunk :cohere {:texts [["Hola"]] :model "embed-multilingual-v3.0" :truncate "END"}) ;; Arroja error 400
  (embed/embed-chunk :cohere {:texts [chunkk] :model "embed-multilingual-v3.0" :truncate "END"}) ;; Arroja error 400. ¿Será por el número de tokens?
  (embed/embed-chunk :cohere {:texts [chunkk] :model "embed-multilingual-v2.0" :truncate "END"})
  (embed/embed-chunk :cohere {:texts [chunkk]  :truncate "END"})

  (require '[cohere.client :as client])
  (client/generate :prompt "Hey, there! What is your name?")
  (client/embed :texts [chunkk])

  (System/getProperty "cohere.api.key")
  
  ;; Hay que hacer una partición de la colección en piezas de 96 items c/u
  (partition 96 splitting)

  (->> (dividir-documentos res)
       (crear-embeddings :cohere))

  (->> (into [] (take 5 (dividir-documentos res)))
       (crear-embeddings :cohere))
  ) 