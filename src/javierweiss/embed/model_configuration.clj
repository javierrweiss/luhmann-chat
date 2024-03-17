(ns javierweiss.embed.model-configuration
  (:require [javierweiss.configuracion.config :refer [configuracion-llm]]
            [libpython-clj2.python :as py :refer [py. py.-]]))

(defmulti setup-model :seleccion)

(defmethod setup-model :cohere [conf]
  (System/setProperty "cohere.api.key" (-> conf :llm-service :cohere-key )))

(defmethod setup-model :openai [conf]
  (let [openai-embedding (py/from-import langchain.embeddings OpenAIEmbeddings)]
    (openai-embedding :openai_api_key (:openai-key conf))))

(defmethod setup-model :custom-model [_]
  (py/from-import llama_index ServiceContext)
  (py. ServiceContext from_defaults :embed_model "local")
  (py/from-import llama_index VectorStoreIndex))

(defmethod setup-model :default [_]
  (throw (IllegalArgumentException. "No existe configuración implementada para el modelo indicado")))
    
(def modelo (setup-model configuracion-llm))

;;(def cohere-embedding "& {:keys [texts model truncate], :or {model \"embed-english-v2.0\", truncate \"END\"}}" embed)
 
(comment 
  
  (System/getProperty "cohere.api.key")
  )
