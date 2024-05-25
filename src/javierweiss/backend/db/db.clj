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
 [_ dims]
 (crear-tabla-archivo-luhmann full-options dims))

(defmethod crea-tabla-principal :azure
  [_ dims]
  (crear-tabla-archivo-luhmann conn-url dims))

(defmethod crea-tabla-principal :default [_ _]
  (throw (IllegalArgumentException. "Opción no implementada")))



(defmulti inserta-registros (fn [conf _ _] (:seleccion conf)))

(defmethod inserta-registros :aws
 [_ dims valores]
 (crear-registro full-options dims valores))

(defmethod inserta-registros :azure
  [_ dims valores]
  (crear-registro conn-url dims valores))

(defmethod inserta-registros :default [_ _]
  (throw (IllegalArgumentException. "Opción no implementada")))



(def buscar "Recibe un vector de embeddings y el algoritmo (:cosine-distance, :l2-distance, :inner-product) a usar para la búsqueda" 
  (partial busqueda configuracion-db))

(defn crear-tabla-384dims 
  "Crea una tabla donde los embeddings tienen una dimensión de 384"
  []
  (crea-tabla-principal configuracion-db 384))

(defn crear-tabla-768dims 
  "Crea una tabla donde los embeddings tienen una dimensión de 768"
  []
   (crea-tabla-principal configuracion-db 768))

(def inserta-en-tabla-384 "Recibe los valores a insertar en la tabla de 384 dimensiones como vector de vectores"
  (partial inserta-registros configuracion-db 384))

(def inserta-en-tabla-768 "Recibe los valores a insertar en la tabla de 768 dimensiones como vector de vectores"
  (partial inserta-registros configuracion-db 768))
   
(comment 
  
  (require '[javierweiss.backend.retrieve.retrieve :refer [emb]])
  
  (buscar (:embeddings emb) :l2-distance)

  (crear-tabla-768dims)

  (crear-tabla-384dims)
  
  (ns-unmap *ns* 'crea-tabla-principal)

  )


 