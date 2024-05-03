(ns javierweiss.backend.db.sql.consultas
  (:require [javierweiss.backend.db.sql.conexion :refer [ejecuta-sentencia]]
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
        emb (->> embeddings_consulta first (interpose ", ") (apply str))
        consulta [(str "SELECT contenido, referencia  
                       FROM archivo_luhmann
                       ORDER BY embedding " algo " '[" emb "]' LIMIT 5")]]
    (try
      (ejecuta-sentencia consulta opts)
      (catch SQLException e (u/log ::excepcion-busqueda-embeds :mensaje (.getMessage e))))))

