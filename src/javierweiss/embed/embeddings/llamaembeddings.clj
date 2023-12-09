(ns javierweiss.embed.embeddings.llamaembeddings
  (:require [libpython-clj2.python :as py :refer [py. py.-]]))

(def service_context (py/from-import llama_index ServiceContext))

(def llama_embedding (py. ServiceContext from_defaults :embed_model "local"))

(def vector_store_index (py/from-import llama_index VectorStoreIndex))

(defn embed
  [documents]
  (py. vector_store_index from_documents documents))
