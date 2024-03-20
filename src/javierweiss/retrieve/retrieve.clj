(ns javierweiss.retrieve.retrieve
  (:require [cohere.client :refer [chat rerank]]
            [javierweiss.db.db :refer [buscar]]
            [javierweiss.embed.embed :refer [embed]]))

;; Vamos a tomar la pregunta del usuario, la vamos a embeber, luego hacemos una búsqueda, luego un reranking y por último introducimos los
;; tres resultados mejor rankeados en el contexto de la respuesta (documents)
#_(defn embeber-pregunta [modelo]
  (case modelo
    :cohere (fn embeber-pregunta-cohere 
              [pregunta] 
              #_(chat {:message pregunta :search_queries_only true})) ;;No me responde con este flag, hay que pensar en una alternativa.
    (throw (IllegalArgumentException. "Modelo no implementado"))))

(defn embeber-y-buscar
   [pregunta]
  (-> [pregunta]
      embed
      :embeddings
      first
      buscar))

(defn inyectar-contexto [top-documents])


(comment
  (def preguntar-con-cohere (embeber-pregunta :cohere))

  (preguntar-con-cohere "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?")

  (tap> (chat  :message "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"
               :search_queries_only true))
  
  (import java.net.http.HttpClient 
          (java.net.http HttpRequest)
          (java.net.http HttpRequest$BodyPublishers)
          java.net.URI
          (java.net.http HttpResponse$BodyHandlers)) 

  (def cliente (doto (HttpClient/newBuilder)
                 (.build)))
  
  (defn crear-request 
    [body]
    (let [req (doto (HttpRequest/newBuilder)
                (.uri (URI/create "https://api.cohere.ai/v1/chat"))
                (.header "Content-Type" "application/json")
                (.POST (HttpRequest$BodyPublishers/ofString body))
                (.build))
          resp (.send cliente req (HttpResponse$BodyHandlers/ofString))]
      {:status (.statusCode resp)
       :body (.body resp)}))
  
  #_(crear-request ) ;; Me falta el header con la autorización
 
  (def r (-> (embed ["¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"])
             :embeddings))
  
  (require '[sweet-array.core :as sa])

  (tap> r)
  
  (sa/new [double] (first r)) ; class clojure.lang.PersistentVector cannot be cast to class java.lang.Character (clojure.lang.PersistentVector is in unnamed module of loader 'app'; java.lang.Character is in module java.base of loader 'bootstrap')

  (embeber-y-buscar "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?")
  
  :rcf) 
