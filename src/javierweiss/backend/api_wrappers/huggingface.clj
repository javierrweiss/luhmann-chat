(ns javierweiss.backend.api-wrappers.huggingface
  (:require
   [javierweiss.backend.configuracion.config :refer [configuracion-llm-huggingface]]
   [javierweiss.backend.api-wrappers.httpclient :refer [GET POST PUT]]
   [javierweiss.backend.utils.json :as json]
   [com.brunobonacci.mulog :as u]))

(def ^:private url "https://api-inference.huggingface.co/models/")
 
(def ^:private token (-> configuracion-llm-huggingface :llm-service :huggingface-key))

(def ^:dynamic model "deepset/roberta-base-squad2")

(defn consultar
  [{:keys [question context] :as body}]
  (POST (str url model) {:oauth-token token
                         :headers {"Content-Type" "application/json"
                                   "Accept" "application/json"}
                         :body (json/encode {:inputs body})}
    (fn [{:keys [status] {:keys [answer error]} :body}]
      (if (== status 200)
        {:respuesta answer}
        (u/log ::error-en-request-huggingface-api-inference :status status :mensaje error)))
    {:async true}))
  

(comment

 (def res (consultar {:question "¿De qué color es el caballo blanco de Simón Bolívar"
                      :context "Simón Bolívar fue un militar y político nacido en Caracas que liberó 5 naciones del dominio español en el siglo XIX"}))

  (tap> @res)
  (let [{:keys [status] {:keys [answer]} :body} {:body {:answer "Respuesta"}
                                                 :status 200}] 
    [answer status]) 

  :rcf)