# jsonpath

a sexy json reading like the xml-sax.


## 适用场景

目前，JSON的读操作多采用了类似XML的DOM方式，
即使只读一个字段，也要全部加载和解析，很不经济。

jsonpath，采用类似XML的SAX方式，按需读取。
特别适合，读取的属性数不超过总属性20%的场景。

jsonpath的特点就是：速度快，资源省，规则简单，没有Bean。

## 不适用场景

  * JSON写操作。
  * JSON序列化和反序列化（Bean<=>String）。
  * JSON读取属性数超过总属性的50%。

## 语法说明

完全兼容 JSON Pointer (RFC 6901)。但扩展了以下语法:

  * 数组（array）的区间和多选语法(`-`,`,`,`*`)。
  * 对象（object）的多选语法(`,`,`*`)。
  * JSON-Like的转义语法，更像json，使用(`"`)括起来。

有关转义，RFC6901使用`~0`=>`～`，`~1`=>`/`的表示法，
可读性不好，如RESTFull的权限配置中会有大量`/`变成`~1`。

    Because the characters '~' (%x7E) and '/' (%x2F) have special
    meanings in JSON Pointer, '~' needs to be encoded as '~0' and '/'
    needs to be encoded as '~1' when these characters appear in a
    reference token.

JSON-Like的转义语法，采用双引号(`"`)表示，和JSON一致。
对key的使用，只需要从JSON中`Ctrl-c,Ctrl-v`就可以了。

### 通常JSON

  * `/store/book/0/author` 获得第1本书的作者(author)  
  * `/store/book/0,2/author` 获得第1和3本书的作者(author) 
  * `/store/book/1-3/author` 获得第2,3和4本书的作者(author) 
  * `/store/book/*/author` 获得所有书的作者(author) 
  * `/store/bicycle/color` 获得bicycle的color

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

### 特殊JSON

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
    "0-2":10,
    "*":11
}
```

JSON-Like(`"`)       | RFC6901 | value(s)
---------------------|---------|---------
`/foo`,`/"foo"`      |`/foo`   |["bar", "baz"]
`/foo/0`,`/"foo"/"0"`|`/foo/0` |"bar"
`/`,`/""`            |`/`      |0
`/"a/b"`             |`/a~1b`  |1
`/c%d`,`/"c%d"`      |`/c%d`   |2
`/e^f`,`/"e^f"`      |`/e^f`   |3
`/g|h`,`/"g|h"`      |`/g|h`   |4
`/i\\j`,`/"i\\j"`    |`/i\\j`  |5
`/k\"l`,`/"k\"l"`    |`/k\"l`  |6
`/ `,`/" "`          |`/ `     |7
`/"m~n"`             |`/m~0n`  |8
`/0,2`,`/"0,2"`      |`/0,2`   |9
`/0-2`,`/"0-2"`      |`/0-2`   |10
`/*`,`/"*"`          |`/*`     |11

## 参考资料

  * [JSON Pointer (RFC 6901)](http://tools.ietf.org/html/rfc6901)
  * [JSON (JavaScript Object Notation)](http://json.org/)