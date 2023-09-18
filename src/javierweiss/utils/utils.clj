(ns javierweiss.utils.utils
   (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]
             [clojure.walk :as w]))

(defn py-obj->clj-map
  [pyobj]
  (w/keywordize-keys
   (into {} (py/get-attr pyobj "__dict__"))))

