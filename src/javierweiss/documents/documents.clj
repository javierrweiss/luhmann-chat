(ns javierweiss.documents.documents
  (:require [com.brunobonacci.mulog :as u]))

(defn crear-documentos
  [list-or-listfn load-fn]
  (u/log ::ingesta-de-documentos :status :inicio)
  (let [obras (if (fn? list-or-listfn) (list-or-listfn) list-or-listfn)]
    (keep identity
          (for [obra obras] (load-fn obra)))))

