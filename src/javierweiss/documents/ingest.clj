(ns javierweiss.documents.ingest
  (:require [com.brunobonacci.mulog :as u]
            [javierweiss.split.splitters.langchainsplitter :as sp]
            [libpython-clj2.python :as py]
            [javierweiss.split.split :refer [split]]
            [javierweiss.embed.embed :as embed]
            [javierweiss.db.db :as db]
            [javierweiss.utils.utils :as ut]
            [javierweiss.documents.documents :refer [crear-documentos]]
            [clojure.string :as string]))

(defn dividir-documento
  [{:keys [documento split-fn splitter] :as state-map}]
  (u/log ::iniciando-division-documento :documento documento)
  (let [division (->> documento
                      (map #(split split-fn splitter %))
                      (map #(map (fn [document] (py/get-attr document :page_content)) %))
                      (apply concat)
                      vec)]
    (assoc state-map :documento-dividido division)))

(defn crear-embeddings
  [{:keys [servicio documento-dividido] :as state-map}]
  (u/log ::iniciando-embedding :servicio servicio)
  (let [embeddings (embed/embed-chunk servicio documento-dividido)]
    (assoc state-map :service-response embeddings)))

(defn- textos-embeddings
  [{:keys [texts embeddings]}]
  (map #(vector %1 %2) texts embeddings))
 
(defn guardar-embeddings
  [{:keys [servicio referencia service-response] :as state-map}]
  (u/log ::guardando-embeddings :servicio servicio :referencia referencia)
  (let [data (if (seq? service-response) 
               (mapv #(select-keys % [:texts :embeddings]) service-response)
               (vector (select-keys service-response [:texts :embeddings])))
        tuplas-texto-embeddings (mapv #(textos-embeddings %) data) ;;Tanto si es un mapa como si es una secuencia de mapas, cada llave tiene una colección de elementos (textos y embeddings) que quiero extraer 
        registros (into [] (for [tupla tuplas-texto-embeddings :let [txt (first tupla)
                                                                     emb (second tupla)]] 
                             [referencia nil txt (count txt) emb]))]
    (db/inserta-registros servicio registros)))

(defn ingerir_documentos
  [document-list servicio split-fn splitter]
  (doseq [document document-list :let [referencia (-> (-> (ut/py-obj->clj-map (ffirst document)) :metadata :source)
                                                      (string/replace % #"\.pdf" "")
                                                      (string/split % #"/")
                                                      last)]] (->> {:documento document
                                                                                           :servicio servicio
                                                                                           :split-fn split-fn
                                                                                           :splitter splitter
                                                                                           :referencia referencia}
                                                                                          dividir-documentos
                                                                                          (crear-embeddings servicio))))
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
  (println contenidos)
  (count (first contenidos))

  (def splitting (split sp/langchain-split-documents sp/token-splitter (first res)))
  (count splitting)
  (count (py/get-attr (first splitting) :page_content))
  (def extension-splits (map (fn [doc] (let [pagina (py/get-attr doc :page_content)]
                                         (count pagina)))
                             splitting))
  (py. sp/token-splitter count_tokens (py/get-attr (first splitting) :page_content))
  (py.. (sp/token-splitter :chunk_size 750 :chunk_overlap 10) (count_tokens (first splitting)))
  (py. (first splitting) tokens)
  extension-splits
  (type splitting)
  (map (fn [document]
         (-> (ut/py-obj->clj-map document)
             :metadata
             :source))
       splitting)
  (map (fn [elem] (-> (ut/py-obj->clj-map elem) :metadata :source)) splitting)
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

  ;; Hay que hacer una partición de la colección en piezas de 96 items c/u
  (def part (partition 96 splitting))
  (count part)
  (count (second part))

  (def resultado (->> (dividir-documentos res)
                      (crear-embeddings :cohere)))

  (def resultado2 (->> (dividir-documentos [(first res)])
                       (crear-embeddings :cohere)))
  (type (first resultado))
  (tap> (first resultado)) 
  (count resultado2)
  (tap> resultado)
  (type resultado)
  (tap> resultado2) 


  (def divs (dividir-documentos res1))
  divs  
  (def ruta (-> (ut/py-obj->clj-map (ffirst res1)) :metadata :source))
  (apply str (string/replace ruta #"\.pdf" "")) 
  (ut/py-obj->clj-map (ffirst res1))
  (py/get-attr (ffirst res1) :metadata) 
  (count divs)  






  ) 