# Gemini [![Build Status](https://travis-ci.org/h4t0n/gemini.svg?branch=master)](https://travis-ci.org/h4t0n/gemini)

> An opinionated framework for auto-magically create CRUD REST APIs (from a dynamic Schema)

## Overview

Gemini is a REST framework fully developed in Java (Spring) for automatically generate CRUD REST APIs starting from a simple DSL.

#### Features:
* Focus on Entity and Relations (REST APIs with ER model)
* Define POST/PUT/GET/DELETE starting from a flat Entity Definition (no nesting/complex schema)
* Automatically handle Data Storage/Foreign Keys/Relations by using persistence managers/drivers (support postgresql)
* Relations are strictly checked and managed by Entity Manager and Logical keys

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

**ATTENTION:** Gemini is not ready for production. It's in a heavy development phase.

## Quick start
Gemini was developed to be used with different storage types. Currently it supports postgresql database storage. 

##### Requirements
* Postgresql 11
* Java 9+
* Gradle 5+

##### Build Executable
Gemini uses SpringBoot. So let's build the standalone executable.
```bash
# from gemini root
gradle buildJar
cd gemini-postgresql/dist
```
Before executing the standalone jar remember that SpringBoot uses the application.properties and we build Gemini with
Postgresql driver.

This is the minimun configuration to fully statup Gemini (from Spring)

```
# application.properties
spring.datasource.url= jdbc:postgresql://localhost:5432/gem
spring.datasource.username=gem
spring.datasource.password=gem
server.port = 8090
```
 
Now you can run the jar. NB: it is fully executable archive built by springboot task.
```
./gemini-postgresql-0.1-SNAPSHOT-boot.jar
```

Gemini automatically setup its internal structures and creates the ```schema/Runtime.at``` file. You can customize this
file with all your entities, by using the Gemini DSL. Then restart the application to see your APIs in action.
 


## License
GNU GENERAL PUBLIC LICENSE Version 3