(ns javierweiss.backend.embed.embed
  (:require [javierweiss.backend.api-wrappers.cohere :as cohere]
            [javierweiss.backend.embed.model-configuration :refer [modelo modelo-alternativo]]
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

(def embed-local
  "Realiza embedding localmente usando Sentence Transformer. Recibe como argumento un vector de strings y devuelve embedding (vector de doubles)"
  (partial modelo-alternativo "sentence-transformers/multi-qa-mpnet-base-dot-v1"))

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


  (def t "SYST. RES. BEHAV. SCI. VOL. 14, 359 – 371 ( 1997 ) ( cid : 106 ) Research Paper The Control of Intransparency1 Niklas Luhmann * 
   Faculty of Sociology, University of Bielefeld, Germany General systems theory shows that the combination of self - 
   referential operations and operational closure ( or the re - entry of output as input ) generates a surplus of possible 
   operations and therefore intransparency of the system for its own operation. The system cannot produce a complete description of itself. 
   It has to cope with its own unresolvable indeterminacy. To be able to operate under such conditions the system has to introduce time. 
   It has to distinguish between its past and its future. It has to use a memory function that includes both remembering and forgetting. 
   And it needs an oscillator function to represent its future. This means, for example, that the future has to be imagined as achieving or 
   not achieving the goals of the system. Even the distinction of past and future is submitted to oscillation in the sense that the future can 
   be similar to the past or not. In this sense the unresolvable indeterminacy or the intransparency of the system for itself can ﬁ")
  
  (-> t (clojure.string/split #"\s") count)

  )