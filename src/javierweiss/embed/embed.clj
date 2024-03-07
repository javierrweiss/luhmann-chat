(ns javierweiss.embed.embed
  (:require [cohere.client :as cohere]
            [javierweiss.embed.model-configuration :refer [modelo]]
            [javierweiss.configuracion.config :refer [configuracion-llm]]
            [libpython-clj2.python :as py :refer [py. py.-]]
            [com.brunobonacci.mulog :as u]))

(defmulti embed-chunk (fn [conf _] (:seleccion conf)))

(defmethod embed-chunk :cohere
  [_ documents]
  (let [len (count documents)]
    (cond
      (and (> len 0) (<= len 96)) (do
                                    (u/log ::generando-embeddings :cantidad-documentos len)
                                    (cohere/embed {:texts documents
                                                   :model "embed-multilingual-v2.0"
                                                   :truncate "END"}))
      (> len 96) (let [docs (partition 96 documents)]
                   (u/log ::generando-embeddings-en-paralelo :cantidad-documentos len)
                   (doall
                    (pmap
                     #(cohere/embed {:texts %
                                     :model "embed-multilingual-v2.0"
                                     :truncate "END"})
                     docs)))
      :else (throw (IllegalArgumentException. "El objeto documento no puede estar vacío"))))) ;; En verdad quiero lanzar una excepción??

(defmethod embed-chunk :custom-model
  [_ documents]
  (py. modelo from_documents documents))

(defmethod embed-chunk :openai
  [_ documents]
  (py. modelo embed_documents 96)) ;; Falta determinar el número de chunks

(def embed (partial embed-chunk configuracion-llm)) 