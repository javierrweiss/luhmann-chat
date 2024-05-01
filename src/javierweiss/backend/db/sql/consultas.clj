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
        emb #_(-> embeddings_consulta into-array str) (str embeddings_consulta)
        consulta (sql/format {:select [:contenido :referencia]
                              :from :archivo-luhmann
                              :order-by [:embedding algo emb]
                              :limit 3})]
    (try
      (ejecuta-sentencia consulta opts)
      (catch SQLException e (u/log ::excepcion-busqueda-embeds :mensaje (.getMessage e))))))


(comment
  (require '[javierweiss.backend.retrieve.retrieve :refer [emb]])
  (sql/format {:select [:contenido :referencia]
               :from :archivo-luhmann
               :order-by [:embedding "<=>" (into-array (:embeddings emb))]
               :limit 3})
  
  :rcf)