(ns javierweiss.db.db
  (:require [javierweiss.db.sql.conexion :as conn :refer [full-options conn-url]]
            [javierweiss.db.sql.consultas :refer [buscar_similitudes]]
            [javierweiss.db.sql.ddl :refer [crear-tabla-archivo-luhmann]]
            [javierweiss.db.sql.dml :refer [crear-registro]]
            [javierweiss.configuracion.config :refer [configuracion]]
            [com.brunobonacci.mulog :as u]))

(def dbs (->> (configuracion) :db keys (apply hash-set)))

(defmulti buscar (fn [db _] (dbs db)))

(defmethod buscar :aws
 [_ embeddings]
 (buscar_similitudes full-options embeddings))

(defmethod buscar :azure
  [_ embeddings]
  (buscar_similitudes conn-url embeddings))

(defmulti crea-tabla-principal (fn [db _] (dbs db)))

(defmethod crea-tabla-principal :aws
 [_ vector-size]
 (crear-tabla-archivo-luhmann full-options vector-size))

(defmethod crea-tabla-principal :azure
  [_ vector-size]
  (crear-tabla-archivo-luhmann conn-url vector-size))

(defmulti inserta-registros (fn [db _] (dbs db)))

(defmethod inserta-registros :aws
 [_ valores]
 (crear-registro full-options valores))

(defmethod inserta-registros :azure
  [_ valores]
  (crear-registro conn-url valores))

   
(comment
  
  (buscar :postgres) 
  (dbs :aws)
  (dbs :weaviate)
  (crea-tabla-principal 768)
  (ns-unmap *ns* 'b)
  )


 