(ns javierweiss.backend.db.sql.ddl
  (:require [javierweiss.backend.db.sql.conexion :refer [ejecuta-sentencia]]
            [honey.sql :as sql]
            [com.brunobonacci.mulog :as u])
  (:import java.sql.SQLException))

(defn crear-tabla-archivo-luhmann
  "Recibe mapa de conexión y el tamaño del vector para el embedding (e.g. para la API de Cohere son 768)"
  [opts]
  (let [enunciado (sql/format {:create-table [:archivo-luhmann :if-not-exists]
                               :with-columns [[:id :bigserial [:not nil]]
                                              [:referencia :varchar]
                                              [:pagina :int]
                                              [:contenido :varchar]
                                              [:tokens :int]
                                              [:embedding :vector] 
                                              [[:primary-key :id]]]})]
    (try
      (ejecuta-sentencia enunciado opts)
      (catch SQLException e (u/log ::excepcion-crea-tabla-archivo-luhmann :mensaje (.getMessage e))))))


(comment 

 (sql/format {:create-table [:archivo-luhmann :if-not-exists]
              :with-columns [[:id :bigserial [:not nil]]
                             [:referencia :varchar]
                             [:pagina :int]
                             [:contenido :varchar]
                             [:tokens :int]
                             [:embedding [:vector 768]]
                             [[:primary-key :id]]]})
  
  )