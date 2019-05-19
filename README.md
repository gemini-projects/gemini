# Gemini

[![Build Status](https://travis-ci.org/h4t0n/gemini.svg?branch=master)](https://travis-ci.org/h4t0n/gemini)
[![Twitter](https://img.shields.io/badge/Twitter-@h4t0n-blue.svg?style=flat)](http://twitter.com/h4t0n)

Gemini is an opinionated REST framework fully developed in Java (Spring) for automatically generate CRUD REST APIs starting from a 
simple Abstract Type Schema definition (called Gemini DSL).

* [Features](#features)
* [Quick Start](#quick-start)
* [DSL and APIs](#dsl-and-apis)
    * [Entity and Logical Keys](#entity-and-logical-keys)
    * [Primitive Types](#primitive-types)
    * [Date And Time](#date-and-time)

## Features
* **REST Resources** (Entities) are defined with the simple **Gemini DSL**
    * Support for simple data types (TEXT, NUMBERS, DATES, TIME ...)
    * Support for custom and complex types (FILE, IMAGE, ...) // WIP
* **Automagical generation** of REST CRUD APIs for each Resource
    * POST - PUT - GET - DELETE are available out of the box
    * Support for custom routes (Gemini Services available)  
* No need to handle Entities and Relations persistence
    * Gemini Entity Manager check records and relations (by using Entity Logical Keys)
    * Gemini Persistence Manager automatically handle storage and data
        * Postgresql Driver available


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

# The followinf routes are automatically available  
# /api/book             GET (list) / POST (single resouce)
# /api/book/{isbn}      GET / PUT / DELETE (single resource)

# /api/author           GET (list) / POST (single resouce)
# /api/author/{isbn}    GET / PUT / DELETE (single resource)

# Where Book has a reference to Author by using its name (logical key)
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
`application.properties` to define the Gemini Postgresql datasource with and for example the server port.

This is a simple config to fully startup Gemini (from Spring)

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


## DSL and APIs

### Entity and Logical Keys

Entities are the most important element of Gemini. They defines:
* REST Resources routes (via the EntityName)
    * `/api/{entityName}` is the default route
    * `GET api/{entityName}` for the list of available resources
    * `POST api/{entityName}` to insert a new one
* Body Fields (and their type)
    * JSON is the out of the BOX supported content type
* Entity Resource Record logical key (with `*`)
    * `/api/{entityName}/{resourseLogicalKey}` is the target route for a single resource
    * `GET /api/{entityName}/{resourseLogicalKey}` to get the single resource
    * `PUT /api/{entityName}/{resourseLogicalKey}` to update/modify the single resource

```
# For example this is a valid Entity

ENTITY SimpleText {
    TEXT   code *
}

# lets insert a resource
$ curl -d '{"code":"thelk"}' -H "Content-Type: application/json" -X POST http://127.0.0.1:8090/api/simpletext -i

  HTTP/1.1 200
  Content-Type: application/json;charset=UTF-8
  
  {"code":"thelk"}
 
# now we can query the previous inserted one
$ curl http://127.0.0.1:8090/api/simpletext/thelk -i
  
  HTTP/1.1 200
  Content-Type: application/json;charset=UTF-8

  {"code":"thelk"}
```

### Primitive Types

Primitive types are numbers, strings and booleans and their aliases.

```
ENTITY PrimitiveTypes {
    TEXT            code *          // TEXT is the single type for Strings  
    DOUBLE          double
    DECIMAL         anotherDouble
    NUMBER          anyNumber       // (any number, no matter if it is a floating point or not)
    LONG            long
    QUANTITY        anotherLong
    BOOL            isOk
}
```

#### Numbers
In Json notation *Number* is a single type. In Gemini it is the same, but it adds some semantic to each field (useful
for validation)

* **DOUBLE** or **DECIMAL**: floating point numbers
* **LONG** or **QUANTITY**: integer numbers
* **NUMBER**: any number no matter if it has decimals or not 

*they may be extended (for example adding only naturals numbers and so on)*

#### Strings and Text
Any string field can be defined with the **TEXT** type. No matter its size, it is a JSON string.

#### Boolean
Boolean is *true* or *false* and can be defined with the **BOOL** type

#### API example

```
$ curl -H "Content-Type: application/json" -X POST http://127.0.0.1:8090/api/primitivetypes -i
  -d '{"code":"logicalKey","anotherDouble":7.77,"double":7.77,"isOk":true,"anotherLong":7,"anyNumber":70,"long":7}'
  
   HTTP/1.1 200
   Content-Type: application/json;charset=UTF-8
   
   {"code":"logicalKey","anotherDouble":7.77,"double":7.77,"isOk":true,"anotherLong":7,"anyNumber":70,"long":7}
```


### Date And Time
Dates and Times are Strings (in JSON notation). Gemini uses the ISO 8601 Data elements and interchange formats standard. 

```
ENTITY DatesEntity {
    TEXT        code *
    DATE        aDate
    TIME        aTime
    DATETIME    aDateTime
}
```

#### Date
**DATE** is the type you can use for a simple local date (no need of timezone).
Out of the box supported JSON API format is `yyyy-MM-dd`.

#### Time
**TIME** is the type you can use for ISO-8601 standard time. ISO-TIME is the out of the box supported format.
So for example the JSON String `09:41:43.973Z` is a valid time.

#### Datetime
**DATETIME** can be used for a date time field. It is a full ISO-8601 standard ISO-DATE-TIME.
So for example `2019-05-19T09:41:30.000Z` is a valid value.

#### API example

```
$ curl -H "Content-Type: application/json" -X POST http://127.0.0.1:8090/api/datestypes -i -d '{"aTime":"11:11:05.759Z","code":"lkdate","aDate":"2019-05-19","aDateTime":"2019-05-19T11:09:58Z"}'
    
    HTTP/1.1 200
    Content-Type: application/json;charset=UTF-8

    {"aTime":"11:11:05.759Z","code":"lkdate","aDate":"2019-05-19","aDateTime":"2019-05-19T11:09:58Z"}
```

### Entity Reference Types

// TODO - WIP

## License
GNU GENERAL PUBLIC LICENSE Version 3
