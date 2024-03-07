(ns javierweiss.db.db
  (:require [javierweiss.db.sql.conexion :as conn :refer [full-options conn-url]]
            [javierweiss.db.sql.consultas :refer [buscar_similitudes]]
            [javierweiss.db.sql.ddl :refer [crear-tabla-archivo-luhmann]]
            [javierweiss.db.sql.dml :refer [crear-registro]]
            [javierweiss.configuracion.config :refer [configuracion-db]]
            [com.brunobonacci.mulog :as u]))

(defmulti busqueda (fn [conf _] (:seleccion conf)))

(defmethod busqueda :aws
 [_ embeddings]
 (buscar_similitudes full-options embeddings))

(defmethod busqueda :azure
  [_ embeddings]
  (buscar_similitudes conn-url embeddings))

(defmethod busqueda :default [_ _]
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



(def buscar (partial busqueda configuracion-db))

(def crear-tabla (partial crea-tabla-principal configuracion-db))

(def insertar (partial inserta-registros configuracion-db))
   
(comment
  
  
  (ns-unmap *ns* 'b)
  )


 