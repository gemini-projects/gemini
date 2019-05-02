# Gemini

> An opinionated framework for auto-magiccaly create CRUD REST APIs (from a dynamic Schema)

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

