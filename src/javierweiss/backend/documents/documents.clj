(ns javierweiss.backend.documents.documents
  (:require [com.brunobonacci.mulog :as u]
            [javierweiss.backend.load.load :refer [load-all-from-storage]])
  (:import java.io.IOException))

(defn crear-documentos
  "Crea documentos a partir de una selección de archivos ubicados en el sistema de almacenamiento.
   Devuelve un LazySeq"
  [list-or-listfn load-fn]
  (u/log ::ingesta-de-documentos :status :inicio)
  (try
    (let [obras (if (fn? list-or-listfn) (list-or-listfn) list-or-listfn)]
      (keep identity
            (for [obra obras] (load-fn obra))))
    (catch IOException e (u/log ::excepcion-ingesta-de-documentos :mensaje (.getMessage e)))))

(defn crear-documentos-de-todo-el-blob
  []
  (u/log ::creando-documentos-a-partir-de-todos-los-archivos-del-blob :status :inicio)
  (try
    (load-all-from-storage)
    (catch IOException e (u/log ::excepcion-creando-documentos-a-partir-de-todos-los-archivos-del-blob
                                :mensaje (.getMessage e)))))
