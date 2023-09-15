(ns javierweiss.sql.conexion
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [javierweiss.configuracion.config :refer [configuracion]])
  (:import com.zaxxer.hikari.HikariDataSource
           java.sql.SQLException))
 
(def conf (configuracion))
  
(defn ejecuta-sentencia
  [sentence]
  (with-open [^HikariDataSource d (connection/->pool com.zaxxer.hikari.HikariDataSource
                                                     {:dbtype (:dbtype conf)
                                                      :db-name (:db-name conf)
                                                      :username (:user conf) 
                                                      :password (:password conf)
                                                      :host (:host conf)})]
    (jdbc/execute! d sentence))) 

(defn activar-extension
  []
  (try
    (ejecuta-sentencia ["CREATE EXTENSION IF NOT EXISTS vector"])
    (catch SQLException e (.getMessage e))))

(comment 
   
  (ejecuta-sentencia ["SELECT 1"])
   (activar-extension)
  (ejecuta-sentencia ["SELECT * FROM pg_extension"])
  )