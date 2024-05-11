(ns javierweiss.backend.embed.embed
  (:require [javierweiss.backend.api-wrappers.cohere :as cohere]
            [javierweiss.backend.embed.model-configuration :refer [modelo]]
            [javierweiss.backend.configuracion.config :refer [configuracion-llm]]
            [libpython-clj2.python :as py :refer [py. py.-]]
            [com.brunobonacci.mulog :as u]))

(defmulti embed-chunk (fn [conf _ _] (:seleccion conf)))

(defmethod embed-chunk :cohere 
  [_ documents tipo-input]
  (let [len (count documents)]
    (cond
      (and (> len 0) (<= len 96)) (do
                                    (u/log ::generando-embeddings :cantidad-documentos len)
                                    (cohere/embed {:texts documents
                                                   :input_type tipo-input
                                                   :model "embed-multilingual-v3.0"
                                                   :truncate "END"}))
      (> len 96) (let [docs (partition 96 documents)]
                   (u/log ::generando-embeddings-en-paralelo :cantidad-documentos len)
                   (doall
                    (pmap                                                                    ;; En verdad quiero usar pmap??
                     #(cohere/embed {:texts %
                                     :input_type tipo-input
                                     :model "embed-multilingual-v3.0"
                                     :truncate "END"})
                     docs)))
      :else (throw (IllegalArgumentException. "El objeto documento no puede estar vacío"))))) ;; En verdad quiero lanzar una excepción??

(defmethod embed-chunk :custom-model
  [_ documents]
  (py. modelo from_documents documents))

(defmethod embed-chunk :openai
  [_ documents]
  (py. modelo embed_documents 96)) ;; Falta determinar el número de chunks

(def embed
  "Recibe `documents` y `tipo-input` (string) que pueder search_document, search_query, classification ó clustering"
  (partial embed-chunk configuracion-llm)) 


(comment
  
  (let [docs (partition 96 (range 1 10000000))] 
    (time (doall
           (pmap  
            #(reduce *' %)
            docs))))
  
  (let [docs (partition 96 (range 1 10000000))]
    (time 
          (map 
           #(reduce *' %) docs)))

  (let [docs (partition 512 (range 1 10000000))]
    (time (doall
           (pmap
            #(reduce *' %)
            docs))))
  

  )