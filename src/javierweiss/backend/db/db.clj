(ns javierweiss.backend.db.db
  (:require [javierweiss.backend.db.sql.conexion :as conn :refer [full-options conn-url]]
            [javierweiss.backend.db.sql.consultas :refer [buscar_similitudes]]
            [javierweiss.backend.db.sql.ddl :refer [crear-tabla-archivo-luhmann]]
            [javierweiss.backend.db.sql.dml :refer [crear-registro]]
            [javierweiss.backend.configuracion.config :refer [configuracion-db]]
            [com.brunobonacci.mulog :as u]))

(defmulti busqueda
  "Recibe un mapa de configuración `conf`, un array de floats `embeddings` y `algoritmo` es una llave entre las siguientes opciones:
             * :cosine-distance
             * :l2-distance
             * :inner-product"
  (fn [conf _ _] (:seleccion conf)))

(defmethod busqueda :aws 
 [_ embeddings algoritmo]
 (buscar_similitudes full-options embeddings algoritmo))

(defmethod busqueda :azure 
  [_ embeddings algoritmo]
  (buscar_similitudes conn-url embeddings algoritmo))

(defmethod busqueda :default [_ _ _]
  (throw (IllegalArgumentException. "Opción no implementada")))



(defmulti crea-tabla-principal (fn [conf _] (:seleccion conf)))

(defmethod crea-tabla-principal :aws
 [_ vector-size]
 (crear-tabla-archivo-luhmann full-options vector-size))

(defmethod crea-tabla-principal :azure
  [_ vector-size]
  (crear-tabla-archivo-luhmann conn-url vector-size))

(defmethod crea-tabla-principal :default [_ _]
  (throw (IllegalArgumentException. "Opción no implementada")))



(defmulti inserta-registros (fn [conf _] (:seleccion conf)))

(defmethod inserta-registros :aws
 [_ valores]
 (crear-registro full-options valores))

(defmethod inserta-registros :azure
  [_ valores]
  (crear-registro conn-url valores))

(defmethod inserta-registros :default [_ _]
  (throw (IllegalArgumentException. "Opción no implementada")))



(def buscar "Recibe un vector de embeddings y el algoritmo (:cosine-distance, :l2-distance, :inner-product) a usar para la búsqueda" 
  (partial busqueda configuracion-db))

(def crear-tabla "Recibe el tamaño del vector" 
  (partial crea-tabla-principal configuracion-db))

(def insertar "Recibe los valores a insertar en la tabla como vector de vectores"
  (partial inserta-registros configuracion-db))
   
(comment 
  (ns-unmap *ns* 'b)
  )


 