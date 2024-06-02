(ns load-experiments
  (:require [javierweiss.backend.split.split :refer [split-by-character split-by-token]]
            [javierweiss.backend.load.load :refer [load-all-from-storage load-document-from-storage]]
            [javierweiss.backend.cloudclients.listados :refer [obras]]
            [javierweiss.backend.utils.utils :refer [py-obj->clj-map]]
            [javierweiss.backend.embed.embed :refer [embed-384]]
            [javierweiss.backend.split.splitters.custom-semantic-search :refer [aplicar-semantic-chunking]]
            [libpython-clj2.python :as py]
            [clojure.java.io :as io]
            [clojure.string :as s])
  (:import org.apache.tika.Tika
           org.apache.tika.exception.TikaException
           org.xml.sax.SAXException
           java.io.IOException
           java.io.InputStream))

(tap> obras)
(count obras)
(def organisation-und-entscheidung (obras 95))
(def organisation-und-entscheidung-docs (load-document-from-storage organisation-und-entscheidung)) 
(def legal-argumentation-doc (load-document-from-storage "N Luhmann Legal argumentation.pdf"))

(tap> organisation-und-entscheidung-docs)
(tap> legal-argumentation-doc)
  
(def legal-argumentation-content (py/py.- (first legal-argumentation-doc) "page_content"))

(def textos (aplicar-semantic-chunking legal-argumentation-content 3 512 384 95))

 (tap> (s/split legal-argumentation-content #"(?<=[.?!\n\n])\s+"))
 
(defn dividir-y-agrupar
  [doc partition-size]
  (as-> doc c
    (s/split c #"(?<=[.?!\n\n])\s+")
    (partition partition-size c)
    (map #(apply str %) c)
    (mapv vector c)))

(def embs (embed-384 (dividir-y-agrupar legal-argumentation-content 3)))

 
(py/py. organisation-und-entscheidung-docs __len__)
(py/py. legal-argumentation-doc __len__)
 
(def organisation-und-entscheidung-map (-> organisation-und-entscheidung-docs first py-obj->clj-map))

(tap> (:page_content organisation-und-entscheidung-map))

(def organisation-und-entscheidung-split-overlap-10 (split-by-token organisation-und-entscheidung-docs))

(def legal-argumentation-split (split-by-token legal-argumentation-doc))

(py/py. organisation-und-entscheidung-split-overlap-10 __len__)
(py/py. legal-argumentation-split __len__)
 
(tap> (map #(py/get-attr % :page_content) legal-argumentation-split))
(->> (map #(py/get-attr % :page_content) legal-argumentation-split)
     (map count))

(tap> [(-> (nth organisation-und-entscheidung-split-overlap-10 5)
           (py/get-attr "page_content"))
       (-> (nth organisation-und-entscheidung-split-overlap-10 6)
           (py/get-attr "page_content"))])

(def organisation-und-entscheidung-split-overlap-150 (split-by-token organisation-und-entscheidung-docs {:overlap 150}))

(py/py. organisation-und-entscheidung-split-overlap-150 __len__)

(tap> [(-> (nth organisation-und-entscheidung-split-overlap-150 5)
           (py/get-attr "page_content"))
       (-> (nth organisation-und-entscheidung-split-overlap-150 6)
           (py/get-attr "page_content"))])  

(def chunk-by-title (py/from-import unstructured.chunking.title chunk_by_title))

(def chunks (chunk-by-title organisation-und-entscheidung-docs))

(tap> (-> chunks first py-obj->clj-map :page_content))

(def partition-pdf (py/from-import unstructured.partition.pdf partition_pdf))

(def particion (partition-pdf "resources/Niklas Luhmann - Organisation und Entscheidung.-Westdeutscher Verlag (2000).pdf"))

(tap> particion)

(defn parsear-con-tika
  [file]
  (with-open [in ^InputStream (io/input-stream file)]  
    (.parseToString (Tika.) in)))

(def tika (Tika.))

(def strm (.parseToString tika (io/input-stream "resources/Niklas Luhmann - Organisation und Entscheidung.-Westdeutscher Verlag (2000).pdf")))

(io/input-stream "resources/Niklas Luhmann - Organisation und Entscheidung.-Westdeutscher Verlag (2000).pdf")
(parsear-con-tika "resources/Niklas Luhmann - Organisation und Entscheidung.-Westdeutscher Verlag (2000).pdf")

;; Me genera este error que no encuentro cómo resolver
;; ; Execution error (ExceptionInInitializerError) at jdk.internal.loader.NativeLibraries/load (NativeLibraries.java:-2).
; Exception java.lang.UnsatisfiedLinkError: /usr/local/sdkman/candidates/java/17.0.9-ms/lib/libawt_xawt.so: libXtst.so.6: cannot open shared object file: No such file or directory [in thread "nREPL-session-32e2f9b2-8ead-431d-84ef-5cfb3763cc64"]

;; Chequear texto Generalized media and the problem of contingency. Aparentemente la causa de que el proceso se cuelgue y el REPL se desconecte es este texto (o ¿el siguiente?)
;; El momento en que se interrumpe es en el evento <guardando-embeddings>