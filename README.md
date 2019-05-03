# Gemini

> An opinionated framework for auto-magically create CRUD REST APIs (from a dynamic Schema)

Gemini is a REST framework fully developed in Java (Spring) for creating CRUD REST APIs defining Entities (Resource) and Relations using a simple DSL.

For example:

```text
# Two Entities Book/Author

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

# /api/author             GET (list) / POST (single resouce)
# /api/author/{isbn}      GET / PUT / DELETE (single resource)
``` 

**ATTENTION:** Gemini is not ready for production. It is in a heavily development phase.

## Setup in 5 minutes



## License
GNU GENERAL PUBLIC LICENSE Version 3