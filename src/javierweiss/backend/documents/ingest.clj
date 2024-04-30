(ns javierweiss.backend.documents.ingest
  (:require [com.brunobonacci.mulog :as u] 
            [libpython-clj2.python :as py]
            [javierweiss.backend.split.split :refer [split-by-character split-by-token]]
            [javierweiss.backend.embed.embed :refer [embed]]
            [javierweiss.backend.db.db :as db]
            [javierweiss.backend.utils.utils :as ut]
            [javierweiss.backend.documents.documents :refer [crear-documentos]]
            [clojure.string :as string])
  (:import java.time.LocalDateTime))

(defn dividir-documento
  [{:keys [documento referencia] :as state-map}]
  (u/log ::iniciando-division-documento :documento referencia)
  (let [division (->> documento
                      split-by-token 
                      (mapv (fn [document] (py/get-attr document :page_content))))]
    (assoc state-map :documento-dividido division)))

(defn crear-embeddings
  [{:keys [documento-dividido] :as state-map}]
  (u/log ::iniciando-embedding)
  (let [embeddings (embed documento-dividido "search_document")]
    (assoc state-map :service-response embeddings)))

(defn- textos-embeddings
  [{:keys [texts embeddings]}] 
  (mapv #(vector %1 %2) texts embeddings))
 
(defn guardar-embeddings
  [{:keys [referencia service-response]}]
  (u/log ::guardando-embeddings :referencia referencia)
  (letfn [(procesar [embs]
                    (let [data (select-keys embs [:texts :embeddings])
                          tuplas-texto-embeddings (textos-embeddings data)             ;;Tanto si es un mapa como si es una secuencia de mapas, cada llave tiene una colección de elementos (textos y embeddings) que quiero extraer 
                          registros (into [] (for [tupla tuplas-texto-embeddings :let [txt (first tupla)
                                                                                       emb (into-array (second tupla))]]
                                               [referencia nil txt (count txt) emb]))]
                      (u/log ::registro-enviado-a-db-para-debug :registro (first registros))
                      (db/insertar registros)))]
    (if (seq? service-response)
      (map procesar service-response)
      (procesar service-response))))

(defn ingerir_documentos
  "Recibe una colección de Clojure (lista, LazySeq o vector) de listas de objetos Python del tipo Document"
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

(defn ingesta-individual
  [doc]
  (let [referencia (-> (-> (ut/py-obj->clj-map (first doc)) :metadata :source)
                       (string/replace #"\.pdf" "")
                       (string/split #"/")
                       last)]
    (try
      (u/log ::ingiriendo-documento :documento referencia :timestamp (System/currentTimeMillis) :fecha (LocalDateTime/now))
      (->> {:documento doc
            :referencia referencia}
           dividir-documento
           crear-embeddings
           guardar-embeddings)
      (catch Exception e (u/log ::error-en-ingesta-de-documents :documento referencia :excepcion (.getMessage e))))))

(comment

  (require '[javierweiss.backend.cloudclients.listados :refer [obras]]
           '[javierweiss.backend.load.load :refer [load-all-from-storage load-document-from-storage]]
           '[libpython-clj2.python :as py :refer [py. py.. py.-]]
           '[javierweiss.backend.embed.embed :as embed]
           '[missionary.core :as m])
  
  (def zeit_und_gedachtnis (obras 3))

  (def lista_obras (->> obras
                        (remove #(= % "Luhmann Zeit und Gedächtnis.pdf"))
                        (filter #(re-seq #"\w+\.pdf" %))))
  (count lista_obras) 
  ;; Load document devuelve un objeto de tipo lista por cada  documento
  (def res (crear-documentos [(last lista_obras)] load-document-from-storage))
  
  (def documentos_luhmann (crear-documentos lista_obras load-document-from-storage))
  
  (defn ingest-sin-guardar
    [doc]
    (let [referencia (-> (-> (ut/py-obj->clj-map (first doc)) :metadata :source)
                         (string/replace #"\.pdf" "")
                         (string/split #"/")
                         last)]
      (try
        (u/log ::ingiriendo-documento :documento referencia :timestamp (System/currentTimeMillis) :fecha (LocalDateTime/now))
        (->> {:documento doc
              :referencia referencia}
             dividir-documento
             crear-embeddings)
        (catch Exception e (u/log ::error-en-ingesta-de-documents :documento referencia :excepcion (.getMessage e))))))
  
  (def ing (ingest-sin-guardar (first res)))
  (tap> ing)
  (seq? ing)

  (guardar-embeddings ing)
  
  (ingerir_documentos res)
 
  (def ingestion2 (m/?
                   (m/reduce (constantly nil)
                             (m/ap (ingesta-individual
                                    (m/?> 20
                                          (m/seed javierweiss.backend.documents.documents/docs)))))))
  ;; :excepcion "find not supported on type: clojure.lang.LazySeq"
  
  (def soziologische_aufklaerung_5 (javierweiss.backend.documents.documents/docs 55)) 
  
  (ingesta-individual soziologische_aufklaerung_5)
  
  (first ingestion2)

  (def embs (->> {:documento (first res)
                  :referencia "Niklas Luhmann, Zeit und Gedächtnis"}
                 dividir-documento
                 crear-embeddings))

  (guardar-embeddings embs)

  (def v (select-keys (:service-response embs) [:texts :embeddings]))
  (def mv (let [t (:texts v)
                e (:embeddings v)]
            (mapv #(vector %1 %2) t e)))

  (tap> (into [] (for [tupla mv :let [txt (first tupla)
                                      emb (second tupla)]]
                   ["Luhmann, Zeit und Gedächtnis" nil txt (count txt) emb])))

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
  (tap> (embed ["Hola ¿cómo estás?" "Hello, how are you?" "Hallo, wie geht's?"] "search_document"))
  
  :rcf) 