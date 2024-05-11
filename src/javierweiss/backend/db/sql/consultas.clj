(ns javierweiss.backend.db.sql.consultas
  (:require [javierweiss.backend.db.sql.conexion :refer [ejecuta-sentencia]]
            [honey.sql :as sql]
            [com.brunobonacci.mulog :as u])
  (:import java.sql.SQLException))

(map sql/register-op! [:<=> :<-> :<#>])

(defn buscar_similitudes
  "Recibe datos de conexión `opts`, array de floats `embeddings_consulta` y una llave entre las siguientes (`algoritmo`):
   :cosine-distance
   :l2-distance
   :inner-product"
  [opts embeddings_consulta algoritmo]
  (let [algo (case algoritmo
               :cosine-distance  :<=>
               :l2-distance  :<->
               :inner-product :<#>)
        emb (->> embeddings_consulta first (interpose ", ") (apply str))
        consulta (sql/format {:select [:contenido :referencia]
                              :from :archivo-luhmann
                              :order-by [[[algo :embedding [:inline emb]]]]
                              :limit 5})]
    (try
      (ejecuta-sentencia consulta opts)
      (catch SQLException e (u/log ::excepcion-busqueda-embeds :mensaje (.getMessage e))))))


(comment
  

  (let [algoritmo :cosine-distance 
        embeddings_consulta [(into [] (take 100 (repeatedly rand)))]
        algo (case algoritmo
               :cosine-distance  :<=>
               :l2-distance  :<->
               :inner-product :<#>)
        emb (->> embeddings_consulta first (interpose ", ") (apply str))
        consulta (sql/format {:select [:contenido :referencia]
                              :from :archivo-luhmann
                              :order-by [[[algo :embedding [:inline emb]]]]
                              :limit 5})]
    consulta)
  
  (sql/format {:select :*
               :from :items
               :order-by [[[:<=> :embedding [:inline (str (range 1 100))]]]]})
 
 :rcf)