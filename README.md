<p align="center">
   <img src="./gemini_logo.png" height="150">
</p>

___

![License](https://img.shields.io/github/license/h4t0n/gemini.svg)
[![Build Status](https://travis-ci.org/h4t0n/gemini.svg?branch=master)](https://travis-ci.org/h4t0n/gemini)
[![Twitter](https://img.shields.io/badge/Twitter-@h4t0n-blue.svg?style=flat)](http://twitter.com/h4t0n)
[![Gitter](https://img.shields.io/gitter/room/gemini-framework/general)](https://gitter.im/gemini-framework/general)
![Last Commit](https://img.shields.io/github/last-commit/h4t0n/gemini.svg)
![Version](https://img.shields.io/github/release/h4t0n/gemini)

Gemini makes REST API development faster. You can create a full backend application in minutes (with no code) and
It is suitable both for enterprise environments and for modern web/mobile application, microservice and MVP.

Gemini is a backend REST framework to automatically create CRUD REST APIs from scratch starting from a simple Schema
 definition called Gemini DSL. Briefly Gemini automatically handles for you:
* **Data Storage**: creating all persistence stuff (tables, relations and so on)
* **API controllers**: creating common REST CRUD controllers for each DSL Entity
* **Swagger API documentation**: creating the openapi language-agnostic interface to RESTful APIs 
* **Authentication**: by using Spring OAuth2 and JWT tokens
* **API callbacks**: to add business logic with ease

<p align="center">
   <img src="./gemini_hiw.gif" height="400">
</p>

**Why use Gemini**:
* It is build on top Spring Framework: you can use both Gemini features and Spring features (for example custom controller)
* Gemini has a revolutionary approach: it is not a code generator, all the resources are handled dynamically (no classes are generated)
* Zero Code if you want simple CRUD API operations
* Event and Handlers can be used to customize common CRUD API methods

## Index

* [Features](#features)
* [Quick Start & Setup](#quick-start--setup)
    * [Environment Setup](#environment-setup)
    * [Start Gemini](#start-gemini)
    * [Once Started](#once-started)
* [Documentation](#documentation)
* [Contribute](#contribute)
* [License](#license)

## Features
* **REST Resources** (Entities) are defined with the simple **Gemini DSL**
    * Support for simple data types (TEXT, NUMBERS, DATES, TIME ...)
    * Support for custom and complex types (FILE, IMAGE, ...) // WIP work in progress
* **Automagical generation** of REST CRUD APIs for each Resource
    * POST - PUT - GET - DELETE are available out of the box
    * Support for custom routes (Gemini Services available)  
* **Relational Data with ease**: no need to handle Entities and Relations persistence
    * Gemini Entity Manager check records and relations
        * You can use Entity Logical Keys in APIs and JSON
    * Gemini Persistence Manager automatically handle storage and data
        * Postgresql Driver available
* **Swagger** OPENAPI documentation generated out of the box


For example lets use the Gemini DSL to define....

```text
# ..two simple Entities: Book and Author. Where * is used to specify the logical key

ENTITY Book {
      TEXT        isbn    *
      TEXT        name
      AUTHOR      author
      DECIMAL     price
}
  
ENTITY Author {
      TEXT    name        *
      DATE    birthdate
}

# The following routes are automatically available  
# /api/book             GET (list) / POST (single resouce)
# /api/book/{isbn}      GET / PUT / DELETE (single resource)

# /api/author           GET (list) / POST (single resouce)
# /api/author/{isbn}    GET / PUT / DELETE (single resource)

# Where Book has a reference to Author by using its name (logical key)
```

## Quick start & Setup
Gemini has a modular structure but the artifact is always a Spring Boot application that you can run everywhere.

### Environment Setup 
#### Requirements
You can setup Gemini in your local machine installing the following requirements:
* Postgresql 11+
* Java 9+
* Gradle 5+

But the easiest way is to use docker and avoid to install other software except Java.

#### Docker Development Environment
With docker you can setup a full development environment with.

Start docker services is simple, docker compose works for you for all the volumes and variables mapping
```bash
# from gemini root
$ cd docker/dev
$ docker-compose up -d

# Environment is now ready
# goto http://127.0.0.1:8081 - PgAdmin
# goto http://127.0.0.1:8082 - SwaggerUI
# follow instruction below to Start Gemini
```

You can find a video tutorial [here](https://vimeo.com/352005400).
 
Inside the ``docker/dev/`` directory you have the docker-compose configuration and a real working directory environment 
that you can use or modify. The default environment has the following services:
* **Postgresql 11+** database 
    * port=5432 - dbname=gemini - user=gemini - pwd=gemini
    * Gemini ``wd/application.properties`` already uses previous parameters
* **PgAdmin** to have a full control of postgres server
    * Exposed on port 8081 (http://127.0.0.1:8081)
    * Login Username and Password are both ``gemini``
    * The ``Gemini - Docker`` server configuration is provided out of the box just type the ``gemini`` password the first time
* **Swagger UI** to consume the Gemini APIs
    * Available on port 8082 (http://127.0.0.1:8082)
    * Gemini autogenerated openapi definitions are automatically loaded where:
        * ALL definition lists all the Gemini resources (also metadata and core resources)
        * RUNTIME definiton lists only the resurces defined in the ``RUNTIME.at`` DSL file


### Start Gemini

When all services are up (only postgresql is required, but use docker is suggested to consume API with swagger) you can
run Gemini as a normal Spring Boot application. You can build the standalone executable or use your preferred IDE. The 
only things to remember is to specify the ``docker/dev/wd/`` as the Java working directory in order to use the already
crafted ``wd/application.properties``.

##### From IDE
First of all open/load the Gemini root gradle configuration. Gemini is made of modules that may have different main
or entry points. ``gemini-postgresql`` for example provides two main:
* *it.at7.gemini.boot.PostgresqlGeminiMain* to start Gemini with Core and Auth Module

You can disable authentication with ``gemini.auth = false`` in ``application.properties``.

**NB:** you can also develop you modules and you Gemini only as a dependency. Contact me if you need it now. I will made
a dedicated documentation section to custom module development (it is a big part of Gemini). Custom modules are required
to provide specific business logic to your APIs (it is a work in progress).

##### Build Executable
Gemini is built on SpringBoot that allow to generate a standalone executable file with all dependencies.
```bash
# from gemini root - to build a standalone jar
gradle bootJar

# from gemini root - to build a standalone EXECUTABLE jar
gradle executableJar
cd gemini-postgresql/dist
```
 
Now you can run the jar. 
```
# for standalone (bootJar) you need to use java -jar 
./gemini-postgresql-0.2.x-standalone.jar

# while for executable (executableJar) you can run it
# works on linux based machines - only some platform supported - take a look at Spring documentation
./gemini-postgresql-0.2.x-executable.jar
```

### Once Started
Gemini automatically setup its internal structures and creates the ``schema/RUNTIME.at`` abstract type file in the
working directory. Now you can customize this file with all your entities, by using the Gemini DSL.
Then restart the application to see your APIs in action.

If you want to easily navigate APIs you can use the Swagger openapi tools. Gemini automatically generate the openapi 3
json file `openapi/schema/runtime.json`

If you are using Docker all the services are already configured to see all the generated files and specifications, enjoy.


## Documentation
To use Gemini features its DSL and its features take a look at the [documentation](doc/README.md).

NB: documentation will be improved, Gemini is in early stage of development. Contact me for info or questions.

## Contribute
Gemini is in early stage of development. The best way to contribute is to checkout the project and try it. Then we can
speak about new features, improvements and roadmap on gitter or social platforms.

## License
Apache License 2.0
