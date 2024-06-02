(ns javierweiss.backend.db.sql.dml
  (:require [javierweiss.backend.db.sql.conexion :refer [ejecuta-sentencia]]
            [honey.sql :as sql]
            [com.brunobonacci.mulog :as u])
  (:import java.sql.SQLException))

(defn crear-registro
  "Debe ingresar un vector de vectores donde cada vector debe indicar valores para los siguientes campos en el mismo orden:
   (referencia pagina contenido tokens embedding) y un mapa de conexión.
   Para el campo embedding, usar la función into-array o double-array para crear un array Java"
  [opts dims valores]
  (let [enunciado (sql/format {:insert-into (-> (str "archivo-luhmann-" dims) keyword)
                               :columns [:referencia :pagina :contenido :tokens :embedding]
                               :values valores})]
    (try
      (ejecuta-sentencia enunciado opts)
      (catch SQLException e (u/log ::excepcion-insercion-archivo-luhmann :mensaje (.getMessage e))))))

