(ns javierweiss.backend.configuracion.config
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io])
  (:import java.lang.IllegalArgumentException))

(def config-map (read-config (io/resource "config.edn")))

(defn obtener-configuracion
  "Recibe dos llaves indicando el servicio y el nombre. Devuelve un mapa de la forma {:servicio mapa-conf :seleccion nombre} con la configuracion seleccionada"
  [servicio nombre]
  (cond
    (not (and (keyword? servicio) (keyword? nombre))) (throw (IllegalArgumentException. "Los argumentos deben ser del tipo keyword"))
    (not (contains? (servicio config-map nil) nombre)) (throw (IllegalArgumentException. (str "El servicio o nombre indicado no se encuentra en el mapa de configuracion")))
    :else {servicio (-> config-map servicio nombre)
           :seleccion nombre}))

(def configuracion-db (obtener-configuracion :db :azure))

(def configuracion-storage (obtener-configuracion :storage-service :azure))

(def configuracion-llm (obtener-configuracion :llm-service :cohere))


(comment

  (obtener-configuracion :db :aws)
  (obtener-configuracion :db :google-cloud)
  (obtener-configuracion :db :azure)
  (obtener-configuracion :storage-service :azure)
  (obtener-configuracion :storage-service :aws)
  (obtener-configuracion :storage-service :google-cloud)
  (obtener-configuracion :llm-service :cohere)
  (obtener-configuracion :llm-service :openai)
  (obtener-configuracion :llm-service :anthropic)
  (obtener-configuracion :llm-servic :cohere)
  (obtener-configuracion :llm-service :coere)
  (obtener-configuracion :llm-service 'cohere)
  (obtener-configuracion "llm-service" :cohere)
  (obtener-configuracion 2 :cohere)

  :rcf) 