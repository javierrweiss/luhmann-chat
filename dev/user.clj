(ns user
  (:require [portal.api :as p]
            [com.brunobonacci.mulog :as u]))

(u/start-publisher! {:type :console})
(def p (p/open {:launcher :vs-code}))
(add-tap #'p/submit)
 
(comment
(p/close)
  
)