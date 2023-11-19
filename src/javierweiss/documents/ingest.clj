(ns javierweiss.documents.ingest
  (:require [com.brunobonacci.mulog :as u]))

(defn crear-documentos
  [list-or-listfn load-fn]
  (u/log ::ingesta-de-documentos :status :inicio)
  (let [obras (if (fn? list-or-listfn) (list-or-listfn) list-or-listfn)]
    (keep identity
          (for [obra obras] (load-fn obra)))))


(comment
   
  (require '[javierweiss.cloudclients.listados :refer [listar-obras]]
           '[javierweiss.load.load :refer [load-document]])

  (def res (crear-documentos (take 2 (listar-obras :azure)) (partial load-document :langchain-azure-singleblob)))

  res 
 
  )