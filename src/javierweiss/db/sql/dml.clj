(ns javierweiss.db.sql.dml
  (:require [javierweiss.sql.conexion :refer [ejecuta-sentencia]]
            [honey.sql :as sql]
            [com.brunobonacci.mulog :as u])
  (:import java.sql.SQLException))

(defn crear-registro
  "Debe ingresar un vector de vectores donde cada vector debe indicar valores para los siguientes campos en el mismo orden:
   referencia pagina contenido tokens embedding.
   Para el campo embedding, usar la función into-array o double-array para crear un array Java"
  [valores]
  (let [enunciado (sql/format {:insert-into :archivo-luhmann
                               :columns [:referencia :pagina :contenido :tokens :embedding]
                               :values valores})]
    (try
      (ejecuta-sentencia enunciado)
      (catch SQLException e (u/log ::excepcion-insercion-archivo-luhmann :mensaje (.getMessage e))))))

