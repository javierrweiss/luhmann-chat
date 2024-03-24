(ns javierweiss.backend.api-wrappers.httpclient
  (:require [org.httpkit.client :as hk-client]
            [javierweiss.backend.utils.json :as json]
            [com.brunobonacci.mulog :as u])
  (:import java.io.IOException))

(defn send-http-request 
  [method url & [request callback async]]
  (u/log ::sending-request :args [method url request callback async])
  (try 
    (if async
         (method url request (if callback 
                               (comp callback json/json-body->clj) 
                               json/json-body->clj))
         (json/json-body->clj @(method url request)))
    (catch IOException e (u/log ::error-en-request :metodo method :url url :async async :request request :mensaje (.getMessage e)))))

(defn GET
  [url & [request callback {:keys [async] :or {async false}}]]
  (send-http-request hk-client/get url request callback async))

(defn POST
  [url & [request callback {:keys [async] :or {async false}}]]
  (send-http-request hk-client/post url request callback async))

(defn PUT
  [url & [request callback {:keys [async] :or {async false}}]]
  (send-http-request hk-client/put url request callback async))


(comment  
   
 (def sol (GET "https://api.publicapis.org/entries" nil nil {:async true}))
  
 @sol  
  (json/json-body->clj @(hk-client/get "https://api.publicapis.org/entries")) 

  (tap> 
   (json/json-body->clj @(hk-client/post "https://api.cohere.ai/v1/chat" {:oauth-token (-> javierweiss.configuracion.config/configuracion-llm :llm-service :cohere-key)
                                                                          :message "¿Quién fue Hans-Georg Gadamer?"})))
 
  (tap> sol)

  (type (-> sol :body)) 

  (tap> (GET "https://api.publicapis.org/entries"))

  (tap> (GET "https://api.publicapis.org/entries" (fn [req] (assoc req :aditional-key 1))))

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
  
  :rcf)
 
