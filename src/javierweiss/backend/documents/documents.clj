(ns javierweiss.backend.documents.documents
  (:require [com.brunobonacci.mulog :as u]
            [javierweiss.backend.load.load :refer [load-all-from-storage load-document-from-storage]])
  (:import java.io.IOException))

(defn crear-documentos
  "Crea documentos a partir de una selección de archivos ubicados en el sistema de almacenamiento.
   Devuelve un LazySeq"
  [list-or-listfn load-fn]
  (u/log ::ingesta-de-documentos :status :inicio) 
  (let [obras (if (fn? list-or-listfn) (list-or-listfn) list-or-listfn)]
    (keep identity
          (for [obra obras]
            (do (u/log ::cargando-documento :documento obra)
                (load-fn obra))))))

(defn crear-documentos-de-todo-el-blob
  []
  (u/log ::creando-documentos-a-partir-de-todos-los-archivos-del-blob :status :inicio)
  (try
    (load-all-from-storage)
    (catch IOException e (u/log ::excepcion-creando-documentos-a-partir-de-todos-los-archivos-del-blob
                                :mensaje (.getMessage e)))))

 
(comment
  (crear-documentos (range 1 101) inc)
  (def x (crear-documentos [1 2 4 'x] (fn [n] (try (inc n) (catch ClassCastException e (.getMessage e))))))

  (require '[missionary.core :as m]
           '[manifold.deferred :as d]
           '[clojure.string :as string])

  (def xf (comp (map #(str "Procesando: " %)) (map #(string/replace % #"\.pdf" ""))))

  (eduction xf javierweiss.backend.documents.ingest/lista_obras)


  (->> (m/ap (str "Procesado: " (m/?> 5 (m/seed javierweiss.backend.documents.ingest/lista_obras))))
       (m/reduce conj)
       m/?)

  (m/? (m/reduce conj (m/eduction (map inc) (m/seed [1 2 3]))))

  (m/? (m/reduce conj (m/eduction xf (m/seed javierweiss.backend.documents.ingest/lista_obras))))

  (let [a (m/ap (str "Procesado: " (m/?> 5 (m/seed javierweiss.backend.documents.ingest/lista_obras))))]
    (m/? (m/reduce conj a)))

  (m/? (m/reduce (constantly nil) (m/ap (m/?> 5 (m/seed javierweiss.backend.documents.ingest/lista_obras))
                                        (println "Hecho"))))

  (def documents (m/ap (-> (m/?> 10 (m/seed javierweiss.backend.documents.ingest/lista_obras))
                           load-document-from-storage)))
  
  (def docs (m/? (m/reduce conj documents)))
  
  (tap> docs)
 
  :rcf)