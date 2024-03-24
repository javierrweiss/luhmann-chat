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
      first
      (buscar algoritmo)))

(defn embeber-y-buscar-con-keywords
  [pregunta algoritmo]
  (let [req (cohere/chat {:message pregunta
                          :search_queries_only true})
        claves (when (== 200 (:status req))
                 (-> req :body :search-queries :text))]
    (-> [claves]
        (embed "search_query")
        first
        (buscar algoritmo))))

;; Se podría hacer como una especie de round-robin usando los tres algoritmos de búsqueda y luego hacer un re-ranking

(comment

  (tap> (cohere/chat {:message "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"
                      :search_queries_only true}))
  
  (embeber-y-buscar-con-keywords "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?" :l2-distance)

  (def r (-> (embed ["¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"])
             :embeddings))

  (tap> r)

  (into-array (first r))

  ;; Para este caso simple, los tres algoritmos trajeron los mismos resultados
  (tap> (embeber-y-buscar "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?" :cosine-distance))
  (tap> (embeber-y-buscar "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?" :l2-distance))
  (tap> (embeber-y-buscar "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?" :inner-product))

  (def ej (embed ["¿Cuál es la relación entre la temporalidad y la conciencia"] :l2-distance))
  (tap> ej)
  (-> ej :body :embeddings)

  :rcf) 
