(ns javierweiss.embed.embeddings.cohereembed
  (:require [cohere.client :refer [embed]]
            [javierweiss.configuracion.config :refer [configuracion]]))

(System/setProperty "cohere.api.key" (-> (configuracion) :llm-service :cohere :cohere-key))

(def cohere-embedding "& {:keys [texts model truncate], :or {model \"embed-english-v2.0\", truncate \"END\"}}" embed)


