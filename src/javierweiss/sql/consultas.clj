(ns javierweiss.sql.consultas
  (:require [javierweiss.sql.conexion :refer [ejecuta-sentencia]]
            [honey.sql :as sql]
            [com.brunobonacci.mulog :as u]
            [sweet-array.core :as sa])
  (:import java.sql.SQLException))

(defn buscar_similitudes
  [embeddings_consulta]
  (let [emb (if (sa/instance? [double] embeddings_consulta)
              embeddings_consulta
              (sa/new [double] embeddings_consulta))
        consulta (sql/format {:select :contenido
                              :from :archivo-luhmann
                              :order-by [:embedding "<=>" emb]
                              :limit 3})]
    (try
      (ejecuta-sentencia consulta)
      (catch SQLException e (u/log ::excepcion-busqueda-embeds :mensaje (.getMessage e))))))


(comment

  (sa/instance? [double] [34.3 43.4 232.3 232.02])
  (type [34.3 43.4 232.3 232.02])
  (def na (sa/new [double] [34.3 43.4 232.3 232.02]))
  (sa/instance? [double] na)

  )