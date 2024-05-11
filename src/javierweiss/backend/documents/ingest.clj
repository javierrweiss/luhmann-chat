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
    (m/ap (->> (m/?> (m/seed lista-documentos))
               load-document-from-storage
               ingesta-individual)))))

(comment

  (require '[javierweiss.backend.cloudclients.listados :refer [obras]]
           '[javierweiss.backend.load.load :refer [load-all-from-storage load-document-from-storage]]
           '[libpython-clj2.python :as py :refer [py. py.. py.-]]
           '[javierweiss.backend.embed.embed :as embed]
           '[missionary.core :as m])
   
 (tap> obras) 
 (def guardados #{"Luhmann limits of steering.pdf"
                 "Luhmann the concept of society.pdf"
                 "Niklas Luhmann-Love_ A Sketch-Polity (2010).pdf"
                 "N Luhmann El origen de la propiedad.pdf"
                 "Luhmann systems as difference.pdf"
                 "N Luhmann Tautology and Paradox in the selfdescription of modern society.pdf"
                 "Luhmann the theory of social systems and its epistemology reply to danilo zolos critical comments.pdf"
                 "N Luhmann Society meaning and Religion 1985.docx"
                 "N Luhmann Soziologische Aufklarung 2.docx"
                 "Luhmann Zeit und Gedächtnis.pdf"
                 "Luhmann the medium of art.pdf"
                 "N Luhmann Der Gleichheitssatz als Form und als Norm.pdf"
                 "N Luhmann The paradoxy of observing systems.pdf"
                 "N Luhmann La economia de la sociedad como sistema autopoiético.pdf"
                 "Luhmann y Roberts the work of art and the selfreproduction of art.pdf"
                 "N Luhmann Zur Komplexität von Entscheidungssituationen.pdf"
                 "N Luhmann Ecological communication Coping with the unknown.pdf"
                 "Luhmann tecnology environment and social risk.pdf"
                 "N Luhmann Deconstruction as Seco.pdf"
                 "N Luhmann Cual es el caso y qué se esconde detrás.pdf"
                 "N Luhmann Strukturaufloesung durch Interaktion.pdf"
                 "Niklas Luhmann-Die neuzeitlichen Wissenschaften und die Phämenologie  -Picus Verlag (1996).pdf"
                 "Luhmann Book review Michel Crozier y Erhard Friedberg l acteur et le systeme.pdf"
                 "Niklas Luhmann Die Ausdifferenzierung von Erkenntnisgewinn.docx"
                 "Luhmann, Niklas-The Control of Intransparency (1997).pdf"
                 "N Luhmann Nomologische Hypothesen funktionale Aequivalenz Limitationalitaet  Zum wissenschaftstheoretischen Verstaendnis des Funktionalismus.pdf"
                 "N Luhmann Causalidad en el Sur.pdf"
                 "N LuhmannComo-se-pueden-observar-estructuras-latentes-1998.pdf"
                 "N Luhmann The Politics of Systems and Environments, Part II The Paradoxy of Observing Systems.pdf"
                 "Luhmann the representation of society within society.pdf"
                 "N Luhmann Sprache und Kommunikationsmedien Ein schieflaufender Vergleich.pdf"
                 "Luhmann on the scientific context of the concept of communication.pdf"
                 "Luhmann Der neue Chef.pdf"
                 "N Luhmann G Zermeño Padilla La forma escritura.pdf"
                 "N Luhmann Generalized media and the problem of contingency.pdf" ;; Excluido por problemas de OCR
                 "Niklas Luhmann-The Reality of the Mass Media (2000).djvu" ;; Excluido por formato
                 })
  (count guardados)
  (def obras-pdf (->> obras (filter #(re-seq #"\w+\.pdf" %))))
  (def obras-no-pdf (->> obras (remove #(re-seq #"\w+\.pdf" %))))
  (count obras-pdf)
  (count obras-no-pdf)
  (def lista_obras (->> obras
                        (remove guardados)))
  (count lista_obras)
  (tap> lista_obras)
  
  (future 
    (println "Inicia carga de datos")
    (procesar-documentos lista_obras)
    (println "La carga de datos finalizó"))

   
;; https://docs.cohere.com/reference/embed
  ;; No olvidar que el máximo de textos por llamado es 96.

  (tap> (embed ["Hola ¿cómo estás?" "Hello, how are you?" "Hallo, wie geht's?"] "search_document"))

  (remove #{"hola" "chao"} ["saludo" "hola" "saludante" "chao" "fiufiu"])

  
  :rcf) 