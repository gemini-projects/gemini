# Gemini

[![Build Status](https://travis-ci.org/h4t0n/gemini.svg?branch=master)](https://travis-ci.org/h4t0n/gemini)
[![Twitter](https://img.shields.io/badge/Twitter-@h4t0n-blue.svg?style=flat)](http://twitter.com/h4t0n)

> An opinionated framework for auto-magically create CRUD REST APIs from a simple Abstract Type Schema

* [Overview](#overview)
* [Quick Start](#quick-start)
* [Gemini DSL](#gemini---dsl)

## Overview

Gemini is a REST framework fully developed in Java (Spring) for automatically generate CRUD REST APIs starting from a 
simple Abstract Type Schema definition (called Gemini DSL).

#### Features:
* REST Resources (Entities) are defined with the simple Gemini DSL
* REST CRUD APIs: POST/PUT/GET/DELETE are generated automatically 
* No need to handel Entity/Relations persincetence, Gemini Persistence Manager automatically handle data for you
  (postgresql driver available)
* Relations are strictly checked and managed by the Gemini Entity Manager (by Entities Logical keys)

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

# Defines 
# /api/book             GET (list) / POST (single resouce)
# /api/book/{isbn}      GET / PUT / DELETE (single resource)

# /api/author           GET (list) / POST (single resouce)
# /api/author/{isbn}    GET / PUT / DELETE (single resource)

# Where Book has a reference to Author by using its name logical key
```

**ATTENTION:** Gemini is a WIP and it is not ready for production environments.

## Quick start
Gemini was developed to be used with different storage drivers. Currently it supports postgresql database storage. 

##### Requirements
* Postgresql 11
* Java 9+
* Gradle 5+

##### Build Executable
Gemini is built on SpringBoot that allow to generate a standalone executable file with all dependencies.
```bash
# from gemini root - to build a standalone jar
gradle bootJar

# from gemini root - to build a standalone EXECUTABLE jar
gradle executableJar
cd gemini-postgresql/dist
```
Before executing the standalone/executable jar remember that SpringBoot need some properties to run. Let's use the
`application.properties` to defini the Gemini Postgresql datasource with and for example the server port.

This is the minimun configuration to fully statup Gemini (from Spring)

```
# application.properties
spring.datasource.url= jdbc:postgresql://localhost:5432/gem
spring.datasource.username=gem
spring.datasource.password=gem
server.port = 8090
```
 
Now you can run the jar. 
```
# for standalone (bootJar) you need to use java -jar 
./java -jar gemini-postgresql-0.1-SNAPSHOT-standalone.jar

# while for executable (executableJar) you can run it
# works on linux based machines - only some platform supported - take a look at Spring documentation
./gemini-postgresql-0.1-SNAPSHOT-boot.jar
```

Gemini automatically setup its internal structures and creates the ```schema/Runtime.at``` abstract type file in the
working directory. Now you can customize this file with all your entities, by using the Gemini DSL.
Then restart the application to see your APIs in action.


## Gemini - DSL

// TODO - WIP - take a look at the IntegrationTest.at resource schema (core-module)

## License
GNU GENERAL PUBLIC LICENSE Version 3
