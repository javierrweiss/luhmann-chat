(ns javierweiss.embed.embeddings.langchainembed
  (:require [libpython-clj2.python :as py :refer [py. py.-]]
            [javierweiss.configuracion.config :refer [configuracion]]))

(def ^:private openai-embedding (py/from-import langchain.embeddings OpenAIEmbeddings)) 

(def ^:private openai-model (openai-embedding :openai_api_key (-> (configuracion) :llm-service :openai :openai-key)))

(defn openai-embed
  [chunks]
  (py. openai-model embed_documents chunks))


(comment
  openai-embedding
  )
 