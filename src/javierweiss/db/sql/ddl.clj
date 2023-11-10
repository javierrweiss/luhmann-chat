(ns javierweiss.db.sql.ddl
  (:require [javierweiss.sql.conexion :refer [ejecuta-sentencia]]
            [honey.sql :as sql]
            [com.brunobonacci.mulog :as u])
  (:import java.sql.SQLException))

(defn crear-tabla-archivo-luhmann
  []
  (let [enunciado (sql/format {:create-table [:archivo-luhmann :if-not-exists]
                               :with-columns [[:id :bigserial [:not nil]]
                                              [:referencia :varchar]
                                              [:pagina :int]
                                              [:contenido :varchar]
                                              [:tokens :int]
                                              [:embedding [:vector 768]] ;; Tamaño definido para la API de Cohere
                                              [[:primary-key :id]]]})]
    (try
      (ejecuta-sentencia enunciado)
      (catch SQLException e (u/log ::excepcion-crea-tabla-archivo-luhmann :mensaje (.getMessage e))))))


(comment 

(crear-tabla-archivo-luhmann) 
  (ejecuta-sentencia ["SELECT * 
                       FROM information_schema.tables
                       WHERE table_schema = 'public' AND table_type='BASE TABLE'"])
  (ejecuta-sentencia ["SELECT *
                       FROM archivo_luhmann"])
  )