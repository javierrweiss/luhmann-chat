(ns user
  (:require [portal.api :as p]))

#_(def p (p/open {:launcher :vs-code}))
(add-tap #'p/submit)
 
(comment
(p/close)
  
)