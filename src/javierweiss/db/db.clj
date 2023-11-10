(ns javierweiss.db.db
  (:require [javierweiss.db.sql.conexion :as conn :refer [full-options conn-url]]
            [javierweiss.db.sql.consultas :refer [buscar_similitudes]]
            [com.brunobonacci.mulog :as u]))

(defn buscar 
  "Devuelve función de búsqueda de similitud que recibe como argumento un embedding. 
   Recibe como parámetro una llave indicando el tipo de implementación"
  [implementacion]
  (condp = implementacion
    :postgres (partial buscar_similitudes full-options)
    (u/log ::implementacion-vector-search-no-soportada :msg "La implementación seleccionada no tiene soporte o no existe")))


(comment
  
  (buscar :postgres)
  
  )


 