(ns javierweiss.backend.chat.chat
  (:require [javierweiss.backend.retrieve.retrieve :refer [embeber-y-buscar]]
            [javierweiss.backend.api-wrappers.cohere :as cohere]
            [javierweiss.backend.configuracion.config :refer [configuracion-llm]]))

(def ^:dynamic *preambulo* "Eres un experimentado académico en el campo de la sociología, en especial, en la teoría de los sistemas de Niklas Luhmann.
                            Estás haciendo el rol de tutor y/o colega, por lo que se requiere que dilucides cuestiones conceptuales y ayudes a los jovenes
                            investigadores a hallar fructíferas líneas de investigación. Asimismo se espera que tengas discusiones profundas y provocadoras 
                            con tus colegas.")

(defmulti chatear (fn [conf _] (:seleccion conf)))

(defmethod chatear :cohere 
  [_ pregunta algoritmo-busqueda]
  (let [[a b c] (embeber-y-buscar pregunta algoritmo-busqueda)]
    (cohere/chat {:preamble *preambulo*
           :message pregunta
           :documents [{:title (:archivo_luhmann/referencia a)
                        :text (:archivo_luhmann/contenido a)}
                       {:title (:archivo_luhmann/referencia b)
                        :text (:archivo_luhmann/contenido a)}
                       {:title (:archivo_luhmann/referencia c)
                        :text (:archivo_luhmann/contenido c)}]})))

(defmethod chatear :default
  [_ _]
  (throw (IllegalArgumentException. "Opción no implementada")))

(def chat (partial chatear configuracion-llm))


(comment
  
  (tap> (chat {:message "¿Cuál es el rol de la conciencia en la comunicación, según Niklas Luhmann?"
               :preamble "You are an experienced lecturer in Sociology, especially in the theory of social systems of Niklas Luhmann. 
                            You should ellucidate conceptual inquiries and guide young researchers to find fruitful research issues."
               :documents [{:title "Zeit und Gedächtnis"
                            :text "dient die Auffälligkeit des Neuen dem Gedächtnis des Systems da - 21 Zu betonen ist : politisch. Juristisch ist es sehr viel schwieriger, 
                                                       aus der Ablehnung von Ansprüchen auf Steigerung ihrer Berechtigung zu schließen. Diese Diskrepanz läßt die übermäßige Juridifizierung der 
                                                       Wertediskussion in Deutschland in bedenklichem Licht erscheinen. Das Recht erinnert zu viel und muß sich deshalb mit diskriminieren den 
                                                       Prozeßchancen helfen. Es vergißt den, der nicht geklagt hatte. 22 Zum Beispiel das, wogegen ein Kunstwerk aufgetreten war : das geringere 
                                                       Können der Vorgängerkunst oder auch die verlorene Perfektion ( Renaissance ) oder, in der Neu zeit, andere Stilpräferenzen oder auch politisch 
                                                       - ideologische Bindungen, die man vor findet. Zu letzterem Beispiele in de Berg / Prangel 1993. 23 So für den Bereich der concept art Baldwin 
                                                       / Harrison / Ramsden 1994. Zeit und Gedächtnis zu, etwas als bemerkenswert zu erinnern. Gedächtnis, und nicht Geniali tät, ist die 
                                                       entscheidende generative Struktur des Kunstsystems. Kunstdinge können eine Gedächtnisfunktion übernehmen, weil Na turdinge ihr Gedächtnis 
                                                       verloren"}
                           {:title "Zeit und Gedächtnis"
                            :text  "das Unbekanntsein der Zukunft als Ressource zu benutzen ( speziell hierzu Shackle 1979 ), etwa durch Verlagerung unlösbarer Probleme in die 
                                                       Zukunft ( Stichwort „ Wachstum \" ) oder ganz allgemein da durch, daß Strukturen durch ( änderbare ) Entscheidungen fixiert werden. Auch hier 
                                                       liegt auf der Hand, daß solche Umdispositionen in den Tem poralstrukturen der Gesellschaft weder psychologisch erklärt noch durch 
                                                       Bewußtseinsprozesse kontrolliert werden können. VII. Auch wenn es seine Absicht gewesen wäre : Kant ist es nicht gelungen, das Problem der 
                                                       Außenwelt sich ganz vom Halse zu schaffen. Wenn so viel Problemlösung der Subjektität des Subjekts zugeschoben war, for mierte sich im 
                                                       Schatten der Theorie das Problem der Intersubjektivität. Vor allem aber konnte die Außenwelt nicht schlicht entropisch, nicht oh ne eigene, 
                                                       subjektunabhängige Differenzen angenommen werden. Nicht nur die Mehrheit von Subjekten, sondern auch die Mehrheit von Dingen an sich wurde 
                                                       zum Problem. Deren „ Mannigfaltigkeit \" mußte vorausge setzt werden. So hat „ die transzendentale Logik ein Mannigfaltiges der Sinnlichkeit 
                                                       a priori vor sich liegen \". Es gehört zu den „"}
                           {:title "Zeit und Gedächtnis"
                            :text "ktuelle Zeitpunkte. Die Frage, wie ihre elementa re Einheit erzeugt wird, läßt sich deshalb nicht durch Angabe einer Zahl oder einer Variable 
                                                       und damit durch Arithmetik oder Algebra 6 beant worten. Wenn man zur Bezeichnung von Zeitpunkten Zahlen braucht, haben sie nur die Funktion 
                                                       von Namen und nicht die Funktion von Re cheneinheiten, und es ist nur der Riesenbedarf für solche Namen, der uns daran hindert, uns mit Worten 
                                                       wie Juli oder Dienstag zu begnügen. Das führt auf die weitere Frage, wie denn bei der Markierung eines Zeitpunk tes ein Vorher und ein Nachher 
                                                       unterschieden werden könne, so daß der Zeitpunkt selbst als ein bloßes „ Dazwischen \", als eine Grenze, als ein „ weder vorher noch nachher 
                                                       \", als ein „ Nichts \" trotzdem einen Orientie rungsw ert erhält. Die Einheit eines Zeitpunktes wird also durch einen Beobachter erzeugt, 
                                                       und der braucht dafür ein Gedächtnis. In evolutionstheoretischer Perspektive könnte man jetzt fragen : Gibt es Vorteile der Verwendung von Zeit
                                                        als Beobachtungsschema - und sei es nur im Bereich des Wahmehmens, also des Sehens einer Bewegung -, 5 6 Im übrigen : ein altes Thema."}]}
                                                        :cosine-distance))
  
  'respuesta "Niklas Luhmann's concept of time and memory demonstrates how the consciousness plays a role in communication by creating an 
                  element of unity between time points. \n\nAccording to Luhmann, the perception of time depends on an observer's consciousness. 
                  Time points are not mathematically divided, but observed and distinguished by the observer's memory. The consciousness here 
                  functions as a boundary between \"before\" and \"after\", allowing the perception of time units.\n\nFurthermore, the role 
                  of consciousness cannot be explained psychologically or controlled by consciousness processes. Luhmann's view on consciousness 
                  and communication suggests that it is a resource that cannot be fully controlled or explained by human awareness."
  
  )