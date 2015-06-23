# jsonpath

a sexy json reading like the xml-sax.

[![Build Status](https://travis-ci.org/moilioncircle/jsonpath.svg?branch=master)](https://travis-ci.org/moilioncircle/jsonpath)
[![Coverage Status](https://coveralls.io/repos/moilioncircle/jsonpath/badge.svg?branch=master)](https://coveralls.io/r/moilioncircle/jsonpath?branch=master)

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

### 数组（array），如：`[1,2,3,4,5]`

  * `:`，区间（range），参考python list。

    - `x:y` 表示从x（含）到y（含），保证顺序。
      `x`可以省略，默认是`0`，即第一个元素。
      `y`可以省略，默认是`-1`，即倒数第一个元素。

    - `-1:-3`，取得`[5,4,3]`，保证倒序。
    - `4:2`，  取得`[5,4,3]`，保证倒序。
    - `-1:2`， 取得`[5,4,3]`，保证倒序。
    - `0:2`，  取得`[1,2,3]`，保证正序。
    - `:-3`，  取得`[1,2,3]`，保证正序。
    - `2:`，   取得`[3,4,5]`，保证正序。
    - `:`，取得`[1,2,3,4,5]`，保证正序。
    - `4:-1`，取得`[5]`，重合只取一个。


  * `,`，多选（select），多选指定key。
    - `-1,-3`，取得`[5,3]`，key一致
    - `4,2`，  取得`[5,3]`，key一致。
    - `-1,2`， 取得`[5,3]`，key一致。
    - `0,2`，  取得`[1,3]`，key一致。
    - `4,-1`， 取得`[5,5]`，重合都取。
    - `0,2,4`， 取得`[1,3,5]`，key一致。。


  * `*`，过滤（filter），对key进行过滤。
    - 只在API编程中支持，默认取全部。

### 对象（object），如`{"a":1,"b":2,"c":3,"d":4}`

  * `,`，多选（select），多选指定key。
    - `a,b`，取得`[1,2]`，key一致
    - `a,c,d`，  取得`[1,3,4]`，key一致。

  * `*`，过滤（filter），对key进行过滤。
    - 只在API编程中支持，默认取全部。

### 转义（escape）

有关转义，RFC6901使用`~0`=>`～`，`~1`=>`/`的表示法。

    Because the characters '~' (%x7E) and '/' (%x2F) have special
    meanings in JSON Pointer, '~' needs to be encoded as '~0' and '/'
    needs to be encoded as '~1' when these characters appear in a
    reference token.

API提供转义工具类，完成path的转义。
如果有路径歧义，采用key优先匹配原则，如

``` json
{
    "0,2":9,
    "0":10,
    "2":11,
    "*":{"key":12},
    "m":{"key":13}
}
```
`/0,2` 得到`9`，而不是`10`和`11`，如果要得到后者，  
则需要分解路径`/0,2`为`/0`,`/2`，使用路径组合实现。

`/*/key` 得到`12`，而不是`12`和`13`。如果要得到后者，  
只能通过过滤器的API实现，如 `read("/*/key",ALL)`。


### 普通JSON

  * `/store/book/0/author` 获得第1本书的作者(author)  
  * `/store/book/0,2/author` 获得第1和3本书的作者(author) 
  * `/store/book/0:2/author` 获得第1,2,3本书的作者(author) 
  * `/store/book/:2/author`  获得第1,2,3本书的作者(author) 
  * `/store/book/-1:-3/author` 获得倒数第1,2,3本书的作者(author) 
  * `/store/book/:-3/author` 获得第1本到倒数第3本（含）之间书的作者(author) 
  * `/store/book/:/author`    获得所有书的作者(author)
  * `/store/book/0:-1/author` 获得所有书的作者(author)
  * `/store/book/*/author`    获得所有书的作者(author)
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
`/0,2`   |9
`/0:2`   |10
`/*`     |11

## 参考资料

  * [JSON Pointer (RFC 6901)](http://tools.ietf.org/html/rfc6901)
  * [JSON (JavaScript Object Notation)](http://json.org/)
