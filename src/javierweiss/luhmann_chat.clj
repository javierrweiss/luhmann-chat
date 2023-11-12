(ns javierweiss.luhmann-chat
  (:require [libpython-clj2.python :as py])
  (:gen-class))

(py/initialize! :python-executable (str (System/getenv "CONDA_DIR") "/envs/luhmann/bin/python3.10")
                :library-path (str (System/getenv "CONDA_DIR") "/envs/lib/libpython3.10.so"))

(defn greet
  "Callable entry point to the application."
  [data]
  (println (str "Hello, " (or (:name data) "World") "!")))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (greet {:name (first args)}))
