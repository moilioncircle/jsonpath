# jsonpath

[![Build Status](https://travis-ci.org/leonchen83/jsonpath.svg?branch=master)](https://travis-ci.org/leonchen83/jsonpath)
[![Coverage Status](https://coveralls.io/repos/leonchen83/jsonpath/badge.svg?branch=master)](https://coveralls.io/r/leonchen83/jsonpath?branch=master)

This is an implementation of JSON pointer[(RFC 6901)](http://tools.ietf.org/html/rfc6901) in Scala which extends  
JSON pointer syntax(add another three keywords `:` `,` `*`).  
This library support 2 ways to access JSON notation data. `string path parser` and `scala DSL`  

## Syntax

#### string path parser:  

Here is a list of supported operators :   

| *Operator* | *Description*                  | *Example*                       |
| ---------- | ------------------------------ | ------------------------------- |
| ``/``      | path split                     | ``/foo``                        |
| ``:``      | array slice(python like)       | ``/-1:-3``(last 3 elements)     |
| ``,``      | collection of names or indices | ``/foo,bar`` or ``/foo/1,-1,2`` |
| ``*``      | wildcard                       | ``/store/book/*``               |

Code example:  

``` scala
val json =
      """
        |[
        |    [
        |        true,
        |        false,
        |        null
        |    ],
        |    {
        |        "abc": 1.233e-10,
        |        "bcd": true,
        |        "b": null
        |    },
        |    {
        |        "": 1.233e-10,
        |        "bcd": true,
        |        "b": 1.23
        |    },
        |    false,
        |    null
        |]
      """.stripMargin
      
val value6 = JSONPointer().reduceRead[List[Any]]("/*/*", json, List(None, Some((e: String) => e.contains("b"))))
assert(value6 === List(List(1.233E-10, true, null), List(true, 1.23)))

val value7 = JSONPointer().reduceRead[Any]("/-3/1", json)
assert(value7 === NotFound)
```

#### scala DSL:

Code example:

``` scala
val json =
      """
        |[
        |    [
        |        true,
        |        false,
        |        null
        |    ],
        |    {
        |        "abc": 1.233e-10,
        |        "bcd": true,
        |        "b": null
        |    },
        |    {
        |        "": 1.233e-10,
        |        "bcd": true,
        |        "b": 1.23
        |    },
        |    false,
        |    null
        |]
      """.stripMargin

val value0 = JSONPointer().read[List[Any]](new Path / -3 /("bcd", ""), json)
assert(value0 === List(true, 1.233E-10))

val value1 = JSONPointer().reduceRead[List[Any]](new Path / * /(*, (e: String) => e.contains("b")), json)
assert(value1 === List(List(1.233E-10, true, null), List(true, 1.23)))

val value2 = JSONPointer().reduceRead[Any](new Path / (1 -> -1) /(*, (_: String) == "b"), json)
assert(value2 === List(null, 1.23))
    
val value3 = JSONPointer().read[Boolean](new Path / -3 /"bcd", json)
assert(value3 === true)
    
val value4 = JSONPointer().reduceRead[List[Any]](new Path /(*, _ < _ -1), json)
assert(value4 === List(JSONArray(List(true, false, null)), JSONObject(Map("abc" -> 1.233E-10, "bcd" -> true, "b" -> null)),JSONObject(Map( ""-> 1.233E-10, "bcd" -> true, "b" -> 1.23)), false))

```

## Escape  

#### string path parser:  

| *Character* | *Escape*                       | *Example*                   |
| ----------- | ------------------------------ | --------------------------- |
|  ``~``      | ``~0`` (RFC6901 compatibility) | ``/~``=>``/~0``             |
|  ``/``      | ``~1`` (RFC6901 compatibility) | ``/a/b``=> ``/a~1b``        |
|  ``,``      | ``~,``                         | ``/foo,bar``=>``/foo~,bar`` |
|  ``*``      | ``~*``                         | ``/store/*``=>``/store/~*`  |

You can use these rule to escape character manual.or you can use helper method `quote` to do these things.  
For example:  
```scala
import Path._
val path = s"/*/${quote("*")}/${quote("abc,bcd")}"
```
#### scala DSL:

When you are using `scala DSL`.you don't need escape any character.  
for example  
```scala
val path = new Path / * / "*" / "abc,bcd"
```
This `path` will compile to string `/*/~*/abc~,bcd`

## Filters

Filters can only used on `*` .as you can see above.  
We provided three filters.two of them used on `JSONArray`.another one used on `JSONObject`  
`JSONArray`: `Int=>Boolean` and `(Int,Int)=>Boolean`  
`JSONObject`: `String=>Boolean`  

`Int=>Boolean` :`Int` represents `JSONArray` index.if result is `true` this index of `JSONArray` will return.  
`(Int,Int)=>Boolean` : first `Int` represents `JSONArray` index.and second `Int` represents `JSONArray` size.  
`String=>Boolean` : `String` represents `JSONObject` key.  

#### string path parser:

```scala
JSONPointer().reduceRead[List[Any]]("/*/*", json, List(None, Some((e: String) => e.contains("b"))))
```
You **MUST** add two filters to the path above.because this path contains two `*`.  
First filter is `None`.represents filter all things.  
Second filter is `Some((e: String) => e.contains("b"))`.represents filter that `key` contains string `"b"`.  

#### scala DSL:  
```scala
new Path / * /(*, (e: String) => e.contains("b"))
```
You don't need add a filter on first `*`.because with default filter is `None`.  

## Examples

#### Normal:

``` json
{
    "store": {
        "book": [
            {
                "category": "reference",
                "author": "Nigel Rees",
                "title": "Sayings of the Century",
                "price": 8.95
            },
            {
                "category": "fiction",
                "author": "Evelyn Waugh",
                "title": "Sword of Honour",
                "price": 12.99
            },
            {
                "category": "fiction",
                "author": "Herman Melville",
                "title": "Moby Dick",
                "isbn": "0-553-21311-3",
                "price": 8.99
            },
            {
                "category": "fiction",
                "author": "J. R. R. Tolkien",
                "title": "The Lord of the Rings",
                "isbn": "0-395-19395-8",
                "price": 22.99
            }
        ],
        "bicycle": {
            "color": "red",
            "price": 19.95
        }
    },
    "expensive": 10
}
```

  * `/store/book/0/author`     =>`"Nigel Rees"`
  * `/store/book/0,2/author`   =>`List("Nigel Rees","Herman Melville")`
  * `/store/book/0:2/author`   =>`List("Nigel Rees","Evelyn Waugh","Herman Melville")`
  * `/store/book/:2/author`    =>`List("Nigel Rees","Evelyn Waugh","Herman Melville")`
  * `/store/book/-1:-3/author` =>`List("J. R. R. Tolkien","Herman Melville","Evelyn Waugh")`
  * `/store/book/:-3/author`   =>`List("Nigel Rees","Evelyn Waugh")`
  * `/store/book/:/author`     =>`List("Nigel Rees","Evelyn Waugh","Herman Melville","J. R. R. Tolkien")`
  * `/store/book/0:-1/author`  =>`List("Nigel Rees","Evelyn Waugh","Herman Melville","J. R. R. Tolkien")`
  * `/store/book/*/author`     =>`List("Nigel Rees","Evelyn Waugh","Herman Melville","J. R. R. Tolkien")`
  * `/store/bicycle/color`     =>`"red"`

#### Special:

``` json
{
    "foo": ["bar", "baz"],
    "": 0,
    "a/b": 1,
    "c%d": 2,
    "e^f": 3,
    "g|h": 4,
    "i\\j": 5,
    "k\"l": 6,
    " ": 7,
    "m~n": 8,
    "0,2":9,
    "0:2":10,
    "*":11
}
```

 RFC6901 | value(s)
---------|---------
`/foo`   |["bar", "baz"]
`/foo/0` |"bar"
`/`      |0
`/a~1b`  |1
`/c%d`   |2
`/e^f`   |3
`/g|h`   |4
`/i\\j`  |5
`/k\"l`  |6
`/ `     |7
`/m~0n`  |8
`/0~,2`  |9
`/0:2`   |10
`/~*`    |11

## References

  * [JSON Pointer (RFC 6901)](http://tools.ietf.org/html/rfc6901)
  * [JSON (JavaScript Object Notation)](http://json.org/)
