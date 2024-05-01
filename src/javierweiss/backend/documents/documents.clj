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
  
  (m/? (m/reduce conj (m/eduction (map #(-> % inc println)) (m/seed [1 2 3]))))
  
 
  :rcf)