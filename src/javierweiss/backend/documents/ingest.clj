(ns javierweiss.backend.documents.ingest
  (:require [com.brunobonacci.mulog :as u] 
            [libpython-clj2.python :as py]
            [javierweiss.backend.split.split :refer [split-by-character split-by-token]]
            [javierweiss.backend.load.load :refer [load-all-from-storage load-document-from-storage]]
            [javierweiss.backend.embed.embed :refer [embed]]
            [javierweiss.backend.db.db :as db]
            [javierweiss.backend.utils.utils :as ut]
            [javierweiss.backend.documents.documents :refer [crear-documentos]]
            [clojure.string :as string]
            [missionary.core :as m])
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
  (letfn [(emparejar-y-guardar [embs]
            (let [data (select-keys embs [:texts :embeddings])
                  tuplas-texto-embeddings (textos-embeddings data)             ;;Tanto si es un mapa como si es una secuencia de mapas, cada llave tiene una colección de elementos (textos y embeddings) que quiero extraer 
                  registros (into [] (for [tupla tuplas-texto-embeddings :let [txt (first tupla)
                                                                               emb (into-array (second tupla))]]
                                       [referencia nil txt (count txt) emb]))]
              (u/log ::registro-enviado-a-db-para-debug :registro (first registros))
              (db/insertar registros)))
          (procesar []
            (if (seq? service-response)
              (map emparejar-y-guardar service-response)
              (emparejar-y-guardar service-response)))]
    (procesar)))

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


(defn procesar-documentos
  [lista-documentos]
  (m/?
   (m/reduce
    (constantly nil)
    (m/ap (->> (m/?> 20 (m/seed lista-documentos))
               load-document-from-storage
               ingesta-individual)))))

(comment

  (require '[javierweiss.backend.cloudclients.listados :refer [obras]]
           '[javierweiss.backend.load.load :refer [load-all-from-storage load-document-from-storage]]
           '[libpython-clj2.python :as py :refer [py. py.. py.-]]
           '[javierweiss.backend.embed.embed :as embed]
           '[missionary.core :as m])

  (def zeit_und_gedachtnis (obras 3)) 

  (def lista_obras (->> obras
                        (remove #{"Luhmann Zeit und Gedächtnis.pdf"
                                  "Niklas_Luhmann_auth._Soziologische_Aufklärung_5_Konstruktivistische_Perspektiven.pdf"
                                  "N Luhmann Zur Komplexität von Entscheidungssituationen"})
                        (filter #(re-seq #"\w+\.pdf" %))))
  (tap> lista_obras)
  ;; Load document devuelve un objeto de tipo lista por cada  documento
  (def res (crear-documentos ["N Luhmann Zur Komplexität von Entscheidungssituationen.pdf"] load-document-from-storage)) 
  (def divs (dividir-documento {:documento (first res) :referencia "N Luhmann Zur Komplexität von Entscheidungssituationen.pdf"}))
  (-> divs :documento-dividido butlast)
  (def documentos_luhmann (crear-documentos lista_obras load-document-from-storage))

  (def ing (ingest-sin-guardar (first res))) 
  (tap> ing)
  (seq? ing)

  (guardar-embeddings ing)

  (ingerir_documentos res)

  (procesar-documentos lista_obras)

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

  (tap> (embed ["Hola ¿cómo estás?" "Hello, how are you?" "Hallo, wie geht's?"] "search_document"))

  (remove #{"hola" "chao"} ["saludo" "hola" "saludante" "chao" "fiufiu"])

  :rcf) 