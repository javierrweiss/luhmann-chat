(ns javierweiss.documents.ingest
  (:require [com.brunobonacci.mulog :as u] 
            [libpython-clj2.python :as py]
            [javierweiss.split.split :refer [split-by-character split-by-token]]
            [javierweiss.embed.embed :refer [embed]]
            [javierweiss.db.db :as db]
            [javierweiss.utils.utils :as ut]
            [javierweiss.documents.documents :refer [crear-documentos]]
            [clojure.string :as string])
  (:import java.time.LocalDateTime))

(defn dividir-documento
  [{:keys [documento referencia] :as state-map}]
  (u/log ::iniciando-division-documento :documento referencia)
  (let [division (->> (ffirst documento)
                      (map #(split-by-token %))
                      (map #(map (fn [document] (py/get-attr document :page_content)) %))
                      (apply concat)
                      vec)]
    (assoc state-map :documento-dividido division)))

(defn crear-embeddings
  [{:keys [documento-dividido] :as state-map}]
  (u/log ::iniciando-embedding)
  (let [embeddings (embed documento-dividido)]
    (assoc state-map :service-response embeddings)))

(defn- textos-embeddings
  [{:keys [texts embeddings]}]
  (map #(vector %1 %2) texts embeddings))
 
(defn guardar-embeddings
  [{:keys [referencia service-response]}]
  (u/log ::guardando-embeddings :referencia referencia)
  (let [data (if (seq? service-response) 
               (mapv #(select-keys % [:texts :embeddings]) service-response)
               (vector (select-keys service-response [:texts :embeddings])))
        tuplas-texto-embeddings (mapv #(textos-embeddings %) data) ;;Tanto si es un mapa como si es una secuencia de mapas, cada llave tiene una colección de elementos (textos y embeddings) que quiero extraer 
        registros (into [] (for [tupla tuplas-texto-embeddings :let [txt (first tupla)
                                                                     emb (second tupla)]] 
                             [referencia nil txt (count txt) emb]))]
    (db/insertar registros)))

(defn ingerir_documentos
  [document-list]
  (u/log ::iniciando-ingesta-documentos)
  (doseq [document document-list :let [referencia (-> (-> (ut/py-obj->clj-map (first document)) :metadata :source)
                                                      (string/replace #"\.pdf" "")
                                                      (string/split #"/")
                                                      last)]]
    (try
      (u/log ::ingiriendo-documento :documento referencia :timestamp (System/currentTimeMillis) :fecha (LocalDateTime/now))
      (->> {:documento document 
            :referencia referencia}
           dividir-documento
           crear-embeddings
           guardar-embeddings)
      (catch Exception e (u/log ::error-en-ingesta-de-documents :documento referencia :excepcion (.getMessage e))))))

(comment

  (require '[javierweiss.cloudclients.listados :refer [listar-obras]]
           '[javierweiss.load.load :refer [load-document]]
           '[libpython-clj2.python :as py :refer [py. py.. py.-]]
           '[javierweiss.embed.embed :as embed])
 
  ;; Load document devuelve un objeto de tipo lista por cada documento
  (def res (crear-documentos (take 2 (listar-obras :azure)) (partial load-document :langchain-azure-singleblob)))
  (def res1 (crear-documentos (take 1 (listar-obras :azure)) (partial load-document :langchain-azure-singleblob)))

  (type (first res))
  (keys (ut/py-obj->clj-map (ffirst res)))
  (:metadata (ut/py-obj->clj-map (ffirst res)))
  (:type (ut/py-obj->clj-map (ffirst res)))
  (py/get-attr (ffirst res) "page_content")
  (count (ffirst res))
  (def contenidos (map (fn [pyobj]
                         (let [elem (first pyobj)]
                           (py/get-attr elem :page_content)))
                       res)) 

  (def splitting (split sp/langchain-split-documents sp/token-splitter (first res)))
  (count splitting)
  (count (py/get-attr (first splitting) :page_content))
  (def extension-splits (map (fn [doc] (let [pagina (py/get-attr doc :page_content)]
                                         (count pagina)))
                             splitting)) 
  extension-splits
  (type splitting) 
  (count (map #(py/get-attr % :page_content) splitting))
  (def chunkk (py.- (py/get-item splitting 0) page_content))
  (println chunkk)

;; https://docs.cohere.com/reference/embed
  ;; No olvidar que el máximo de textos por llamado es 96.
  (py/->py-list [chunkk])
  (count chunkk)
  (first chunkk)

  (require '[cohere.client :as client])
  (client/generate :prompt "Hey, there! What is your name?")
  (client/embed :texts [chunkk])
  (client/embed {:texts ["Hola ¿cómo estás?" "Hello, how are you?" "Hallo, wie geht's?"]
                 :model "embed-multilingual-v2.0"
                 :truncante "END"})
  (client/embed {:texts ["Hola ¿cómo estás?" "Hello, how are you?" "Hallo, wie geht's?" "Bonjour! ca va!"]
                 :model "embed-multilingual-light-v3.0"
                 #_:truncante #_"END"})
  (System/getProperty "cohere.api.key")
  
  (ingerir_documentos res1 :cohere sp/langchain-split-documents sp/token-splitter)


  ) 