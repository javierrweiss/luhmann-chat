(ns javierweiss.backend.retrieve.retrieve
  (:require [javierweiss.backend.db.db :refer [buscar]]
            [javierweiss.backend.embed.embed :refer [embed]]
            [javierweiss.backend.api-wrappers.cohere :as cohere]))

;; Vamos a tomar la pregunta del usuario, la vamos a embeber, luego hacemos una búsqueda, (luego un reranking?) y por último introducimos los
;; tres resultados mejor rankeados en el contexto de la respuesta (documents)

(defn embeber-y-buscar
   [pregunta algoritmo]
  (-> [pregunta]
      (embed "search_query") 
      :embeddings
      (buscar algoritmo)))

(defn embeber-y-buscar-con-keywords
  [pregunta algoritmo]
  (let [req (cohere/chat {:message pregunta
                          :search_queries_only true})
        claves (some->> req :search-queries (map :text))]
    (some-> claves
            vec
            (embed "search_query")
            :embeddings
            (buscar algoritmo))))
 
;; Se podría hacer como una especie de round-robin usando los tres algoritmos de búsqueda y luego hacer un re-ranking

(comment
  
  (def emb (-> ["¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"]
               (embed "search_query")))
  
  (def emb2 (-> ["¿Cuál es el concepto fundamental de la sociología de Luhmann?"]
               (embed "search_query")))
  
  (def emb3 (-> ["¿Qué significa sociedad?"]
                (embed "search_query")))
  
  (let [conciencia (-> ["¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"]
                       (embed "search_query")
                       :embeddings)
        concepto_fundamental (-> ["¿Cuál es el concepto fundamental de la sociología de Luhmann?"]
                                 (embed "search_query")
                                 :embeddings)]
    (= conciencia concepto_fundamental))
   
  (type (into-array (seq (:embeddings emb))))

  (type (:embeddings emb))

  (tap> (cohere/chat {:message "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"
                      :search_queries_only true}))
    
  (tap> (embeber-y-buscar-con-keywords "Welche Rolle spielt das Bewusstsein in die Kommunikation?" :cosine-distance))

  (def r (-> (embed ["¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"])
             :embeddings))

  (tap> r)

  (into-array (first r))
 
  ;; Para este caso simple, los tres algoritmos trajeron los mismos resultados  
  (def r (embeber-y-buscar "¿Cuál es el concepto fundamental de la sociología de Luhmann?" :cosine-distance))
  (tap> (embeber-y-buscar "¿Qué es la teoría de los sistemas?" :cosine-distance))
  ;;Execution error (IllegalArgumentException) at java.lang.reflect.Array/set (Array.java:-2).
; array element type mismatch
  (tap> (embeber-y-buscar "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?" :l2-distance))
  (tap> (embeber-y-buscar "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?" :inner-product))

  (def ej (embed ["¿Cuál es la relación entre la temporalidad y la conciencia"] :l2-distance))
  (tap> ej)
  (-> ej :body :embeddings)

  :rcf) 
