(ns javierweiss.documents.split
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]))

(def splitter (py/from-import langchain.text_splitter CharacterTextSplitter))

(def token-splitter (py/from-import langchain.text_splitter TokenTextSplitter))