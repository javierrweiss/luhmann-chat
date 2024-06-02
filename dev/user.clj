(ns user
  (:require [portal.api :as p]
            [com.brunobonacci.mulog :as u]
            [libpython-clj2.python :as py]))

(py/initialize! :python-executable (str (or (System/getenv "CONDA_DIR") (str (System/getenv "HOME") "/conda")) "/envs/luhmann/bin/python3.10")
                :library-path (str (or (System/getenv "CONDA_DIR") (str (System/getenv "HOME") "/conda")) "/envs/lib/libpython3.10.so"))
(u/start-publisher! {:type :console}) 
(def p (p/open {:launcher :vs-code}))
(add-tap #'p/submit)   
#_(require '[flow-storm.api :as fs-api])
#_(fs-api/local-connect) ;; falla porque estamos en un container
 
(comment
  (p/close) 
  (tap> (+ 1 1)) 
  
  *repl*
  (sync-deps)
  (add-lib 'cnuernber/ham-fisted)
  )