(ns javierweiss.backend.db.sql.consultas
  (:require [javierweiss.backend.db.sql.conexion :refer [ejecuta-sentencia]]
            [honey.sql :as sql]
            [com.brunobonacci.mulog :as u])
  (:import java.sql.SQLException))

(defn buscar_similitudes
  "Recibe datos de conexión `opts`, array de floats `embeddings_consulta` y una llave entre las siguientes (`algoritmo`):
   :cosine-distance
   :l2-distance
   :inner-product"
  [opts embeddings_consulta algoritmo]
  (let [algo (case algoritmo
               :cosine-distance "<=>" 
               :l2-distance "<->"
               :inner-product "<#>")
        emb (-> embeddings_consulta into-array)
        consulta (sql/format {:select [:contenido :referencia]
                              :from :archivo-luhmann
                              :order-by [:embedding algo emb]
                              :limit 3})]
    (try
      (ejecuta-sentencia consulta opts)
      (catch SQLException e (u/log ::excepcion-busqueda-embeds :mensaje (.getMessage e))))))


(comment

  (sa/instance? [double] [34.3 43.4 232.3 232.02])
  (type [34.3 43.4 232.3 232.02])
  (def na (sa/new [double] [34.3 43.4 232.3 232.02]))
  (sa/instance? [double] na)
  (let [emb (into-array (first javierweiss.retrieve.retrieve/r))
        opts (:db javierweiss.configuracion.config/configuracion-db)
        consulta (sql/format {:select :contenido
                              :from :archivo-luhmann
                              :order-by [:embedding "<=>" emb]
                              :limit 3})]
    (ejecuta-sentencia consulta opts))
#_(if (sa/instance? [double] embeddings_consulta)
    embeddings_consulta
    (sa/new [double] embeddings_consulta))
  ) 