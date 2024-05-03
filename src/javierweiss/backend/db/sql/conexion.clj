(ns javierweiss.backend.db.sql.conexion
  (:require [next.jdbc :as jdbc]
            [next.jdbc.connection :as connection]
            [javierweiss.backend.configuracion.config :refer [configuracion-db]])
  (:import com.zaxxer.hikari.HikariDataSource
           java.sql.SQLException))
 
(def conf (-> configuracion-db :db))

(def full-options {:dbtype (:dbtype conf)
                   :db-name (:db-name conf)
                   :username (:user conf)
                   :password (:password conf)
                   :host (:host conf)
                   :port (or (:port conf) 5432)})
     
(def conn-url {:jdbcUrl  (:jdbc-url conf) 
               :username (:user conf)
               :password (:password conf)})  
   
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
  (tap> (ejecuta-sentencia ["SELECT pg_available_extensions()"] conn-url))
  (activar-extension full-options)
  (activar-extension conn-url)
  (ejecuta-sentencia ["show azure.extensions"] conn-url) 
  (ejecuta-sentencia ["SELECT * FROM pg_extension"] conn-url)
  (ejecuta-sentencia ["ALTER EXTENSION vector UPDATE"] conn-url)
  (connection/->pool com.zaxxer.hikari.HikariDataSource conn-url)
  (defonce ds (jdbc/get-datasource {:jdbcUrl (:jdbc-url conf)}))
  (jdbc/execute! ds ["SELECT NOW()"])
  (ejecuta-sentencia ["SELECT NOW()"] conn-url)
;; Crear índice 
  (ejecuta-sentencia ["SET maintenance_work_mem = '8GB'"] conn-url)
  (ejecuta-sentencia ["CREATE INDEX ON archivo_luhmann USING hnsw (embedding vector_cosine_ops)"] conn-url)

  (ejecuta-sentencia ["SELECT phase, round(100.0 * blocks_done / nullif(blocks_total, 0), 1) AS percent FROM pg_stat_progress_create_index"] conn-url)
  (ejecuta-sentencia ["SELECT * FROM pg_indexes WHERE tablename = 'archivo_luhmann'"] conn-url)


  (ejecuta-sentencia ["SELECT * 
                         FROM information_schema.tables
                         WHERE table_schema = 'public' AND table_type='BASE TABLE'"] conn-url)
  (ejecuta-sentencia ["SELECT column_name, 
                              data_type, 
                              character_maximum_length, 
                              is_nullable, 
                              column_default 
                      FROM information_schema.columns 
                      WHERE table_name = 'archivo_luhmann'"] conn-url)
  (tap> (ejecuta-sentencia ["SELECT *
                         FROM archivo_luhmann"] conn-url))
  (tap> (ejecuta-sentencia ["SELECT DISTINCT(referencia)
                           FROM archivo_luhmann"] conn-url))
  (tap> (ejecuta-sentencia ["SELECT referencia, COUNT(*) AS Numero_de_referencias
                            FROM archivo_luhmann
                            GROUP BY referencia"] conn-url))

  (require '[honey.sql :as sql]
           '[javierweiss.backend.retrieve.retrieve :refer [emb emb2 emb3]]
           '[next.jdbc.types :as types]
           '[clojure.string :as string])
  
  (sql/register-op! :<=> :ignore-nil true)

  (count (:embeddings emb))
  
  (let [sql1 (sql/format {:select [:contenido :referencia]
                          :from :archivo-luhmann
                          :order-by [:<-> :embedding  (:embeddings emb)]
                          :limit 5}) 
        sql2 (sql/format {:select [:contenido :referencia]
                          :from :archivo-luhmann
                          :order-by  [[:embedding :<=>] (:embeddings emb)]
                          :limit 3})
        sql3  (sql/format {:select [:contenido :referencia] 
                           :from :archivo-luhmann
                           :order-by [[:embedding ] (:embeddings emb)] 
                           :limit 3}) 
        sql4 [(str "SELECT contenido, referencia  
               FROM archivo_luhmann
               ORDER BY embedding <=> '[" (apply str (interpose ", " (first (:embeddings emb2)))) "]' LIMIT 5")]
        sql5 [(str "SELECT contenido, referencia
               FROM archivo_luhmann
               ORDER BY 1 - (embedding <=> '[" 
                   (apply str (interpose ", " (first (:embeddings emb2))))
                   "]') LIMIT 5")]]
    (tap> (ejecuta-sentencia sql5 conn-url))
    #_(tap> (ejecuta-sentencia sql2 conn-url))
    #_(tap> (ejecuta-sentencia sql3 conn-url)))
  
  
  
  (count (:embeddings emb))

  (= (:embeddings emb2) (:embeddings emb))
  (= (:embeddings emb2) (:embeddings emb3))
  (= (:embeddings emb3) (:embeddings emb))
  (= (:embeddings emb2) (:embeddings emb3) (:embeddings emb))

  (sql/format {:order-by [:embedding [:<=> :?emb]]} {:params {:emb (:embeddings emb)}})

  :rcf)   