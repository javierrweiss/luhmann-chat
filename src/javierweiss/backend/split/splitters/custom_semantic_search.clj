(ns javierweiss.backend.split.splitters.custom-semantic-search
  (:require [clojure.string :as s]
            [javierweiss.backend.embed.embed :as embeddings]
            [fastmath.distance :refer [cosine]]
            [fastmath.stats :refer [percentile]]))

(defn dividir-y-agrupar
  "Recibe un string representando un documento completo `doc` el cual se dividirá en oraciones y el tamaño de la partición `partition-size` representa 
   la cantidad de oraciones que serán agrupadas. El tamaño de la partición debe estar en corcondacia con la dimensión del vector del modelo de embedding 
   que se va a emplear."
  [doc partition-size]
  (as-> doc c
    (s/split c #"(?<=[.?!\n\n])\s+")
    (partition partition-size c)
    (map #(apply str %) c)
    (mapv vector c)))

(defn get-embeddings
  [embed-size chunks]
  (let [embed-fn (case embed-size
                   384 embeddings/embed-384
                   764 embeddings/embed
                   1024 embeddings/embed-1024
                   nil)
        incrustaciones (embed-fn chunks)]
    (mapv
     (fn [chunk incrustacion]
       {:texto chunk
        :embedding incrustacion})
     chunks
     incrustaciones)))

(defn calcular-distancias
  [chunks-vec]
  (->> chunks-vec
       (map :embedding)
       (partition 2)
       (map (fn [[a b]] (cosine a b)))
       (map #(- 1 %))))

(defn agregar-distancias
  [chunks-vec]
  (let [distancias (calcular-distancias chunks-vec)
        dist (interleave distancias distancias)]
    (mapv (fn [chunk-map distance]
           (assoc chunk-map :distancia distance))
         chunks-vec
         dist)))
 
(defn aplicar-semantic-chunking
  [documento tamano-particion max-chunk-length embedding-size percentil]
  (let [raw-chunks (dividir-y-agrupar documento tamano-particion)
        chunks-clasificados (->> raw-chunks
                                 (get-embeddings embedding-size)
                                 agregar-distancias)
        distancia-umbral (as-> chunks-clasificados chunks
                           (mapv :distancia chunks)
                           (percentile chunks percentil))
        particion-final (partition-by
                         (fn [chunk]
                           (when (or (>= (count (:texto chunk)) max-chunk-length) (> (:distancia chunk) distancia-umbral))
                             true))
                         chunks-clasificados)
        textos (->> particion-final 
                    (map #(map :texto %))
                    (map #(interpose " " %)))]
    (mapv #(apply str %) textos)))


(comment   
   (map (fn [[a b]] (cosine a b)) [(partition 10 (take 20 (repeatedly rand)))])
  (def t [{:a 1 :b 2} {:a 3 :b 6} {:a 112 :b 2} {:a 24 :b 2} ])
  (partition-by (fn [elem] (> 10 (:a elem))) t)
  (partition-by
   (fn [chunk]
      (> 5 (:distancia chunk)))
   [{:distancia 1 :texto "Hola"} {:distancia 6 :texto "Hola"} {:distancia 1 :texto "Hola"} {:distancia 7 :texto "Hola"} {:distancia 1 :texto "Hola"} {:distancia 2 :texto "Hola"}])
  
  (merge-with (fn [a b]
                (if (and (string? a) (string? b))
                  (str a " " b)
                  b)) 
              {:distancia 1 :texto "Hola"} {:distancia 2 :texto "Hola"})
  
  (map #(apply str %)
       (map #(interpose " " %)
            (map #(map :texto %) [[{:texto "foree" :a 43} {:texto "fordsee" :a 43}] 
                                  [{:texto "foresdse" :a 43} {:texto "forreee" :a 43}]
                                  [{:texto "foresdse" :a 43}]
                                  [{:texto "foqqqree" :a 43} {:texto "for34ee" :a 43} {:texto "34foree" :a 43}]])))
   


:rcf)