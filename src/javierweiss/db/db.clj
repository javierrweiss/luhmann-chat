(ns javierweiss.db.db
  (:require [javierweiss.db.sql.conexion :as conn :refer [full-options conn-url]]
            [javierweiss.db.sql.consultas :refer [buscar_similitudes]]
            [javierweiss.db.sql.ddl :refer [crear-tabla-archivo-luhmann]]
            [javierweiss.db.sql.dml :refer [crear-registro]]
            [com.brunobonacci.mulog :as u]))

(defn buscar 
  "Recibe como parámetro una llave indicando el tipo de implementación.
   Devuelve función de búsqueda de similitud que recibe como argumento un embedding.
   Debe coincidir con el tipo de base de vectores empleada"
  [implementacion]
  (condp = implementacion
    :postgres (partial buscar_similitudes conn-url)
    (u/log ::implementacion-vector-search-no-soportada :msg "La implementación seleccionada no tiene soporte o no existe")))

(defn crea-tabla-principal
  [vector-size]
  (crear-tabla-archivo-luhmann conn-url vector-size))
  
(defn inserta-registros
  "Recibe como parámetro una llave indicando el tipo de implementación de base de datos de vectores.
   Devuelve una función para insertar registros en la misma."
  [implementacion]
  (condp = implementacion 
    :postgres (partial crear-registro conn-url)
    (u/log ::implementacion-vector-db-no-soportada :msg "La implementación seleccionada no tiene soporte o no existe")))


 
(comment
  
  (buscar :postgres) 
  (crea-tabla-principal 768)
  )


 