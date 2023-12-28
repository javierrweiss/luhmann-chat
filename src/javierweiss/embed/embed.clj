(ns javierweiss.embed.embed
  (:require [javierweiss.embed.embeddings.cohereembed :as cohere]
            [com.brunobonacci.mulog :as u]))

(def embedding-service #{:langchain :cohere :llama})

(defmulti embed-chunk (fn [service _] (embedding-service service)))

(defmethod embed-chunk :cohere
  [_ documents]
  (let [len (count documents)]
    (cond
      (and (> len 0) (<= len 96)) (do
                                    (u/log ::generando-embeddings :cantidad-documentos len)
                                    (cohere/cohere-embedding {:texts documents
                                                              :truncate "END"}))
      (> len 96) (let [docs (partition 96 documents)]
                   (u/log ::generando-embeddings-en-paralelo :cantidad-documentos len)
                   (doall
                    (pmap
                     #(cohere/cohere-embedding {:texts %
                                                :truncate "END"})
                     docs)))
      :else (throw (IllegalArgumentException. "El objeto documento no puede estar vacío"))))) ;; En verdad quiero lanzar una excepción??

