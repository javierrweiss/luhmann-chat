(ns javierweiss.documents.ingest
  (:require [com.brunobonacci.mulog :as u]))

(defn crear-documentos
  [list-fn load-fn]
  (u/log ::ingesta-de-documentos :status :inicio)
  (when-let [obras (list-fn)]
    (keep identity
          (for [obra obras] (load-fn obra)))))


(comment
  
 
  )