(ns javierweiss.db.sql.conexion
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [javierweiss.configuracion.config :refer [configuracion]])
  (:import com.zaxxer.hikari.HikariDataSource
           java.sql.SQLException))
 
(def conf (-> (configuracion) :db :railway))

(def full-options {:dbtype (:dbtype conf)
                   :db-name (:db-name conf)
                   :username (:user conf)
                   :password (:password conf)
                   :host (:host conf)
                   :port (or (:port conf) 5432)})
   
(def conn-url {:jdbcUrl (:jdbc-url conf)})

(defn ejecuta-sentencia
  [sentence opts]
  (with-open [^HikariDataSource d (connection/->pool com.zaxxer.hikari.HikariDataSource opts)]
    (jdbc/execute! d sentence))) 

(defn activar-extension
  [opts]
  (try
    (ejecuta-sentencia ["CREATE EXTENSION IF NOT EXISTS vector"] opts)
    (catch SQLException e (.getMessage e))))

(comment 
   
  (ejecuta-sentencia ["SELECT 1"] conn-url)
  (ejecuta-sentencia ["SELECT 1"] full-options)
  (tap> (ejecuta-sentencia ["SELECT pg_available_extensions()"] full-options))
   (activar-extension full-options)
  (ejecuta-sentencia ["SELECT * FROM pg_extension"] full-options)
 
  (defonce ds (jdbc/get-datasource {:jdbcUrl (:jdbc-url conf)}))
 (connection/->pool com.zaxxer.hikari.HikariDataSource {:jdbcUrl (:jdbc-url conf)})
  (jdbc/execute! ds ["SELECT 1"])
  ) 