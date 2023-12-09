# luhmann-chat

*luhmann-chat* es una prueba de concepto de una aplicación que usa RAG (*Retrieval Augmented Generation*) para chatear con una base de conocimiento específica, a saber, la teoría de los sistemas del sociólogo alemán Niklas Luhmann.

## Diseño

Esta aplicación cuenta con los siguientes componentes:

+ Un servicio de almacenamiento (S3, Azure Blob Storage, etc.) para almacenar los archivos que representan parte de la obra de Niklas Luhmann en distintos idiomas (inglés, español y alemán).

+ Una base de datos de vectores ó postgresql empleando la extensión pgvector. 

+ Una plataforma proveedora de los servicios de LLM o modelos opensource.

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

### En Codespaces

Este proyecto cuenta con un archivo devcontainer.json donde se definen las principales dependencias que el proyecto necesita para ejecutarse. 

Basta sólo activar el entorno *luhmann* cuando se comience a trabajar y conectarse al REPL.

### En Gitpod

Ejecutar el script *create_env.sh* para crear el entorno virtual. Luego activar el entorno (asegúrese de crear una nueva terminal): 

```bash
conda activate luhmann
```

Y finalmente conectarse al REPL.

## TAREAS PENDIENTES
- [ ] Necesitamos idear una forma de validar las lecturas del OCR
- [ ] Realizar los embeddings 
- [ X ] Crear interfaces y módulos para las distintas fases del proceso de recuperación 


## Licencia

Este desarrollo se inscribe dentro del proyecto de investigación **Recursos computacionales para investigación en torno a la Teoría de Sistemas Sociales** financiado por la Universidad de Flores (UFLO).

Copyright © 2023 Jrivero

_EPLv1.0 is just the default for projects generated by `deps-new`: you are not_
_required to open source this project, nor are you required to use EPLv1.0!_
_Feel free to remove or change the `LICENSE` file and remove or update this_
_section of the `README.md` file!_

Distributed under the Eclipse Public License version 1.0.

<a name="nota">1</a>: Esta imagen fue tomada del curso de DeepLearning.AI titulado [*Langchain Chat with your Data*](https://learn.deeplearning.ai/langchain-chat-with-your-data).
