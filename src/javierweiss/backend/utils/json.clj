(ns javierweiss.backend.utils.json
  (:require [jsonista.core :refer [read-value write-value-as-string object-mapper]]
            [camel-snake-kebab.core :as csk]
            [clojure.string :as string]
            [com.brunobonacci.mulog :as u]))

(def mapper (object-mapper {:encode-key-fn csk/->snake_case_string 
                            :decode-key-fn csk/->kebab-case-keyword 
                            :pretty true
                            :strip-nils true}))

(defn encode
  [object]
  (when object 
    (write-value-as-string object mapper)))

(defn decode
  [json-string]
  (when-not (string/blank? json-string) 
   (read-value json-string mapper)))

(defn json-body->clj
  [request]
  (if (and (map? request) (contains? request :body)) 
    (update request :body decode)
    (do
      (u/log ::error-en-parseo-cuerpo-json :objeto-recibido request)
      (throw (IllegalArgumentException. "Esta función requiere un mapa con una llave :body")))))


(comment 
  
  (def resp {:body "{\"count\":1427,\"entries\":[1,2,3,4]}"
             :headers {}
             :opts []})
  (decode "")
  (decode (:body resp))
  (encode {:a 3 :b 334})
  ((comp nil json-body->clj) resp) (contains? resp :body)
  (write-value-as-string [] mapper)
  )