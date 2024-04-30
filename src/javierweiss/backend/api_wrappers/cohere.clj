(ns javierweiss.backend.api-wrappers.cohere
  (:require [javierweiss.backend.configuracion.config :refer [configuracion-llm]]
            [javierweiss.backend.api-wrappers.httpclient :refer [GET POST PUT]]
            [javierweiss.backend.utils.json :as json]
            [com.brunobonacci.mulog :as u]))

(def ^:private url "https://api.cohere.ai/v1") 

(def ^:private token (-> configuracion-llm :llm-service :cohere-key))

(defn chat
  "https://docs.cohere.com/reference/chat"
  [{:keys [message
           model
           stream
           preamble
           chat_history
           conversation_id
           prompt_truncation
           connectors
           search_queries_only
           documents
           temperature
           max_tokens
           k
           p
           seed
           frequency_penalty
           presence_penalty
           tools
           tool_results] 
    :or {stream false
         temperature 0.8}}]
  {:pre [(some? message)]}
  (let [params (cond-> {}
                 message (assoc :message message)
                 model (assoc :model model)
                 stream (assoc :stream stream)
                 preamble (assoc :preamble preamble)
                 chat_history (assoc :chat_history chat_history)
                 conversation_id (assoc :conversation_id conversation_id)
                 prompt_truncation (assoc :prompt_truncation prompt_truncation)
                 connectors (assoc :connectors connectors)
                 search_queries_only (assoc :search_queries_only search_queries_only)
                 documents (assoc :documents documents)
                 temperature (assoc :temperature temperature)
                 max_tokens (assoc :max_tokens max_tokens)
                 k (assoc :k k)
                 p (assoc :p p)
                 seed (assoc :seed seed)
                 frequency_penalty (assoc :frequency_penalty frequency_penalty)
                 presence_penalty (assoc :presence_penalty presence_penalty)
                 tools (assoc :tools tools)
                 tool_results (assoc :tool_results tool_results))]
    (POST (str url "/chat") {:oauth-token token
                             :headers {"Content-Type" "application/json"
                                       "Accept" "application/json"}
                             :body (json/encode params)})))


(defn embed
  "https://docs.cohere.com/reference/embed
   Modelos disponibles:
   * embed-english-v3.0, dimension 1024
   * embed-multilingual-v3.0, dimension 1024
   * embed-english-light-v3.0, dimension 384
   * embed-multilingual-light-v3.0, dimension 384
   * embed-english-v2.0, dimension 4096
   * embed-english-light-v2.0, dimension 1024
   * embed-multilingual-v2.0, dimension 768
   Input types (string):
   * search_document (para documentos)
   * search_query (para consultas)
   * classification
   * clustering"
  [{:keys [texts
           model
           input_type
           embedding_types
           truncate]
    :or {model "embed-multilingual-v2.0"}}]
  {:pre [(some? texts)]}
  (let [params (cond-> {}
                 texts (assoc :texts texts)
                 model (assoc :model model)
                 input_type (assoc :input-type input_type)
                 embedding_types (assoc :embedding_types embedding_types)
                 truncate (assoc :truncate truncate))
        {:keys [status body error]} (POST (str url "/embed") {:oauth-token token
                                       :headers {"Content-Type" "application/json"
                                                 "Accept" "application/json"}
                                       :body (json/encode params)})]
    (if (== status 200)
      (let [{:keys [embeddings texts]} body]
        {:embeddings embeddings
         :texts texts})
      (u/log ::error-en-request-embedding :status status :mensaje error))))


(comment
  (let [message "Hola"
        model nil
        stream nil
        preamble nil
        chat_history ["Historia" "del" "chat"]
        conversation_id nil
        prompt_truncation nil
        connectors nil
        search_queries_only nil
        documents nil
        temperature nil
        max_tokens nil
        k nil
        p nil
        seed nil
        frequency_penalty nil
        presence_penalty nil
        tools ["Muchas" 1 3 4 'si {:d :ffd}]
        tool_results nil]
    (cond-> {}
      message (assoc :message message)
      model (assoc :model model)
      stream (assoc :stream stream)
      preamble (assoc :preamble preamble)
      chat_history (assoc :chat_history chat_history)
      conversation_id (assoc :conversation_id conversation_id)
      prompt_truncation (assoc :prompt_truncation prompt_truncation)
      connectors (assoc :connectors connectors)
      search_queries_only (assoc :search_queries_only search_queries_only)
      documents (assoc :documents documents)
      temperature (assoc :temperature temperature)
      max_tokens (assoc :max_tokens max_tokens)
      k (assoc :k k)
      p (assoc :p p)
      seed (assoc :seed seed)
      frequency_penalty (assoc :frequency_penalty frequency_penalty)
      presence_penalty (assoc :presence_penalty presence_penalty)
      tools (assoc :tools tools)
      tool_results (assoc :tool_results tool_results)))  
   
  (tap> (POST (str url "/chat") {:headers {"Content-Type" "application/json"
                                           "Accept" "application/json" 
                                           "Authorization" (str "Bearer " token)} 
                                 :body (json/encode {:message "¿Quién fue Georg Simmel?"})}))
  
  (tap> (POST (str url "/chat") {:oauth-token token 
                                 :headers {"Content-Type" "application/json"
                                           "Accept" "application/json"}
                                 :body (json/encode {:message "¿Quién fue Georg Simmel?"})}))
  
  (tap> (POST (str url "/embed") {:oauth-token token
                                 :headers {"Content-Type" "application/json"
                                           "Accept" "application/json"}
                                 :body (json/encode {:texts ["¿Quién fue Georg Simmel?"]
                                                     :input_type "search_query"})}
          (fn [{:keys [status body error]}]
            (if (== status 200)
              body #_(:embeddings body)
              (u/log ::error-en-request-embedding :status status :mensaje error)))))  
  ) 