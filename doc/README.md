## Index

* [DSL and APIs](#dsl-and-apis)
    * [Entity and Logical Keys](#entity-and-logical-keys)
    * [Primitive Types](#primitive-types)
    * [Date And Time](#date-and-time)
    * [Entity Reference](#entity-reference-types)
* [Modules](#modules)
    * [Core Module](#gemini-core)
        * [Entity and Field](#entity-and-field)
        * [Gemini Meta Data](#gemini-meta-data)
            * [UUID-V3](#uuid-v3)
    * [Auth Module](#auth-module)
    * [Runtime Module](#runtime-module)
    
    
    
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

Each defined Entity (with a logical key) is itself a Gemini Type. 

```
ENTITY Tag {
    TEXT    name    *
    TEXT    description
}

# Tag is a type that we can use in the Post Entity

ENTITY Post {
    LONG postId *
    TEXT message
    Tag  tag
}
```

#### API Example - Insert Tag and Post

First of all we need at least one tag.
```
$ curl -H "Content-Type: application/json" -X POST http://127.0.0.1:8090/api/tag -i -d '{"name":"italy","description":"We ❤️ Italy"}'

    HTTP/1.1 200
    Content-Type: application/json;charset=UTF-8

    {"name":"italy","description":"We ❤️ Italy"}
```

To insert the POST only the logical key of Tag is required. 

```
$ curl -H "Content-Type: application/json" -X POST http://127.0.0.1:8090/api/post -i -d '{"postid":1,"message":"Rome", "tag": "italy"}'

    HTTP/1.1 200
    Content-Type: application/json;charset=UTF-8

    {"tag":"italy","postId":1,"message":"Rome"}
```

#### API Example - Post with unknown Tag

What if we try to insert a Post with an unknown tag?. Not found error!!!

```
$ curl -H "Content-Type: application/json" -X POST http://127.0.0.1:8090/api/post -i -d '{"postid":2,"message":"Rome", "tag": "unknown"}'

    HTTP/1.1 404
```

### Arrays

Arrays are a Work in Progress. Gemini will supports array out of the box either from primitive types or complex types. 
Actually is supported only the TEXT array.

```
ENTITY Messages {
    LONG messageId *
    [TEXT] message
}
```

## Modules

### Gemini CORE Module
Core is the main module of Gemini and it defines some features, and predefined entities (and fields) available also
for developers. Take a look at the `Core.at` abstract type schema.

#### ENTITY and FIELD


Gemini tracks all the Entities using the ENTITY Entity. All fields instead are stored in the FIELD Entity. 
It looks like a work game, but let's query them.

```
# The list of all available entities
$ curl http://127.0.0.1:8090/api/entity -i

# The list of all available fields
$ curl http://127.0.0.1:8090/api/field -i
```

By convention, entity names are always expressed (and stored) in UPPERCASE notation. While fields are handled in
LOWERCASE notation. 

#### Gemini META data
Gemini handle meta data for each operation performed on a single EntityRecord. You can retrieve these meta information
using the Gemini header.

```
Gemini: gemini.api

The JSON response has the following schema
{
    "meta": {...}
    "data": {...}
}
```

NB: Core metadata are defined inside the `Core.at` schema and by default information about created and modified time
are automatically stored by the framework.


Let's query for example the `PrimitiveTypes` defines earlier.

```
$ curl -H "Gemini: gemini.api" http://127.0.0.1:8090/api/primitivetypes/test -i
    HTTP/1.1 200
    Gemini: gemini.api
    Content-Type: application/json;charset=UTF-8

    {
        "data":{"code":"test","anotherDouble":0.01,"double":0.2,"isOk":true,"anotherLong":2,"anyNumber":0.01,"long":1},
        "meta":{
            "created":"2019-05-19T14:49:57.765151Z",
            "modified":"2019-05-19T14:49:57.765151Z",
            "uuid":"97907b5d-854a-34ff-92ef-251055adba2a"
         }
     }
```

##### UUID-V3

Gemini automatically create a unique ID for each record by using UUID-v3 ids generated starting from the logical key.

You can query resources also by their UUID, especially when the logical key is made by multiple fields.

```
# note that there are not meta because we have not inserted the Gemini header in the request
# The uuid the same returned from the previuous example

$ curl  http://127.0.0.1:8090/api/primitivetypes/97907b5d-854a-34ff-92ef-251055adba2a -i

    HTTP/1.1 200
    Content-Type: application/json;charset=UTF-8

    {"code":"test","anotherDouble":0.01,"double":0.2,"isOk":true,"anotherLong":2,"anyNumber":0.01,"long":1}

```

Take a look at this article for some clarification [Rest API: UUID-V3 is the right way](https://medium.com/@h4t0n/rest-api-uuid-v3-is-the-right-way-3ca0695610dc)

### Auth Module
// TODO - Soon in v0.2.x

### Runtime Module
// TODO
