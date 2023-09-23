# luhmann-chat

*luhmann-chat* es una prueba de concepto de una aplicación que usa RAG (*Retrieval Augmented Generation*) para chatear con una base de conocimiento específica, a saber, la teoría de los sistemas del sociólogo alemán Niklas Luhmann.

## Diseño

Esta aplicación cuenta con los siguientes componentes:

+ Un bucket S3 para almacenar los archivos que representan parte de la obra de Niklas Luhmann en distintos idiomas (inglés, español y alemán).

+ Una base de datos postgresql como base de datos de vectores empleando la extensión pgvector. Actualmente una instancia de AWS Aurora.

+ Uso de la plataforma Cohere como proveedora de los servicios de LLM.

### Arquitectura de un RAG

Implementar un RAG requiere completar los siguientes 8 pasos básicos:

1. Ingerir los documentos.
2. Dividir los documentos en trozos que puedan ser procesados por el LLM (*Large Language Model*).
3. Convertir en tokens las cadenas de texto y contar los tokens para que calcen dentro del límite tolerado por la API del LLM.
4. Crear *embeddings*  de esos documentos ya reducidos en tamaño.
5. Almacenar esos *embeddings* en una base de datos de vectores.
6. Tomar el input del usuario y crear un *embedding* del mismo. 
7. Emplear algún algoritmo de similaridad para recuperar registros en la base de datos de vectores.
8. Pasarle los resultados al modelo del LLM como contexto para producir una respuesta.

   ![](resources/RAG-flow.jpeg) <sup>[1](#nota)</sup>

## Uso

En desarrollo...

## Desarrollo

Este proyecto usa libpython-clj. Para usar libpython-clj hay que tener en cuenta qué entorno de Python está instalado en nuestro computador.
Si no tenemos ni Conda, ni pyenv, sencillamente podemos hacer lo siguiente:

```clojure 
  (ns xxx.xxx
  (:require [libpython-clj2.require :refer [require-python]]
            [libpython-clj2.python :refer [py. py.. py.-] :as py]))
 ```
 Si, en cambio tenemos Conda, debemos hacerlo así si no tenemos un entorno virtual activado:

 ```clojure
 (ns xxx.xxx
  (:require [libpython-clj2.python :as py :refer [py. py.. py.-]]))

  (py/initialize! :python-executable (str (System/getenv "CONDA_DIR") "/bin/python3.10") 
                  :library-path (str (System/getenv "CONDA_DIR") "/lib/libpython3.10.so"))
 ```
  O así, si tenemos un entorno virtual activado:
```clojure 
  (ns xxx.xxx
  (:require [libpython-clj2.python :as py]))
  
  (py/initialize! :python-executable "/opt/anaconda3/envs/my_env/bin/python3.7"
                :library-path "/opt/anaconda3/envs/my_env/lib/libpython3.7m.so")
```
Siempre es mejor tener un entorno virtual. Con Conda hacer lo siguiente:

 ```bash
 conda create -n <nombre-del-entorno>
 ```
 
 ```bash
 conda init bash
 ```

 ```bash
 conda activate <nombre-del-entorno>
 ```

Y si tenemos pyenv, debemos seguir estas instrucciones: https://clj-python.github.io/libpython-clj/environments.html

Y en todo caso, si nuestro JDK es java 17, debemos crear un alias en nuestro deps.edn con lo siguiente:

```edn 
:jvm-opts ["--add-modules" "jdk.incubator.foreign"
           "--enable-native-access=ALL-UNNAMED"]
```
Para importar paquetes primero hay que instalarlos con el gestor de paquetes que se esté usando, sea pip o conda. 

## TAREAS PENDIENTES
- [ ] Necesitamos idear una forma de validar las lecturas del OCR
- [ ] Debemos revisar la función de carga de documentos para que sin importar que algun documento arroje excepción devuelva los demás
- [ ] Realizar los embeddings con la API de Cohere
- [ ] ¿Usar migratus para gestionar el SQL? (No es prioridad)


## Licencia

Este desarrollo se inscribe dentro del proyecto de investigación **Recursos computacionales para investigación en torno a la Teoría de Sistemas Sociales** financiado por la Universidad de Flores (UFLO).

Copyright © 2023 Jrivero

_EPLv1.0 is just the default for projects generated by `deps-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.

<a name="nota">1</a>: Esta imagen fue tomada del curso de DeepLearning.AI titulado [*Langchain Chat with your Data*](https://learn.deeplearning.ai/langchain-chat-with-your-data).
