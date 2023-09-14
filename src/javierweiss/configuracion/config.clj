(ns javierweiss.configuracion.config
  (:require [aero.core :refer [read-config]]
            [clojure.java.io :as io]))

(defn configuracion []
  (read-config (io/resource "config.edn")))
 
(comment
  (configuracion)
  )