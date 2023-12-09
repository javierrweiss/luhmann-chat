(ns javierweiss.embed.embed
  (:require [javierweiss.embed.embeddings.cohereembed :as cohere]))

(def embedding-service #{:langchain :cohere :llama})

(defmulti embed-chunk (fn [service _] (embedding-service service)))

(defmethod embed-chunk :cohere
 [_ documents]
 (cohere/cohere-embedding documents))