package com.moilioncircle.jsonpath

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * Created by leon on 15-6-1.
 */
@RunWith(classOf[JUnitRunner])
class JSONPathTest extends FunSuite {
  test("json parser 10") {
    val json = "[\"\\u9648\"]"
    val parser = JSONParser(json)
    val value = parser.parser()
    assert(value == JSONArray(List("陈")))

    {
      val json = "[\"\\u964"
      val parser = JSONParser(json)
      try {
        parser.parser()
        fail()
      } catch {
        case e: JSONSyntaxException =>
      }
    }

    {
      val json = ""
      val parser = JSONParser(json)
      try {
        parser.parser()
        fail()
      } catch {
        case e: JSONSyntaxException =>
      }
    }
  }

  test("json parser") {
    val json =
      """
        |[
        |    155e+012,
        |    2,
        |    3,
        |    [
        |        true,
        |        false,
        |        null
        |    ],
        |    {
        |        "a\u9648bc": 1.233e-10,
        |        "bcd": true,
        |        "c\rde": null
        |    },
        |    true,
        |    false,
        |    null
        |]
      """.stripMargin
    val parser = JSONParser(json)
    parser.parser() match {
      case JSONArray(list: List[Any]) => assert(list.toString === "List(1.55E14, 2, 3, JSONArray(List(true, false, null)), JSONObject(Map(a陈bc -> 1.233E-10, bcd -> true, c\rde -> null)), true, false, null)")
    }
  }

  test("json parser 1") {
    val json =
      """
        |[
        |    155e+012,
        |    0.55,
        |    0,
        |    9,
        |    100,
        |    110e+12,
        |    110e12,
        |    110e-12,
        |    3,
        |    -100,
        |    [
        |        true,
        |        false,
        |        nulul
        |    ],
        |    {
        |        "a泉bc": 1.233e-10,
        |        "bcd": true,
        |        "c\rde": null
        |    },
        |    true,
        |    false,
        |    null
        |]
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parser()
      fail()
    } catch {
      case e: JSONLexerException => //pass test
    }
  }

  test("json parser 2") {
    val json =
      """
        |[
        |    155e+012,
        |    2,
        |    3,
        |    [
        |        truue,
        |        false,
        |        null
        |    ],
        |    {
        |        "a泉bc": 1.233e-10,
        |        "bcd": true,
        |        "c\rde": null
        |    },
        |    true,
        |    false,
        |    null
        |]
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parser()
      fail()
    } catch {
      case e: JSONLexerException => //pass test
    }
  }

  test("json parser 3") {
    val json =
      """
        |[
        |    155e+012,
        |    2,
        |    3,
        |    [
        |        true,
        |        falsse,
        |        null
        |    ],
        |    {
        |        "a泉bc": 1.233e-10,
        |        "bcd": true,
        |        "c\rde": null
        |    },
        |    true,
        |    false,
        |    null
        |]
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parser()
      fail()
    } catch {
      case e: JSONLexerException => //pass test
    }
  }

  test("json parser 4") {
    val json =
      """
        |[
        |    1a5e+012,
        |    2,
        |    3,
        |    [
        |        true,
        |        falsse,
        |        null
        |    ],
        |    {
        |        "a泉bc": 1.233e-10,
        |        "bcd": true,
        |        "c\rde": null
        |    },
        |    true,
        |    false,
        |    null
        |]
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parser()
      fail()
    } catch {
      case e: JSONSyntaxException => //pass test
    }
  }

  test("json parser 5") {
    val json =
      """
        |{
        |]
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parser()
      fail()
    } catch {
      case e: JSONSyntaxException => //pass test
    }
  }

  test("json parser 6") {
    val json =
      """
        |true
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parser()
      fail()
    } catch {
      case e: JSONSyntaxException => //pass test
    }
  }

  test("json parser 7") {
    val json =
      """
        |{"a":true "b":false}
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parser()
      fail()
    } catch {
      case e: JSONSyntaxException => //pass test
    }

    val json1 =
      """
        |{"a":true,"b":false
      """.stripMargin
    val parser1 = JSONParser(json1)
    try {
      parser1.parser()
      fail()
    } catch {
      case e: JSONSyntaxException => //pass test
    }

    val json2 =
      """
        |[true,false
      """.stripMargin
    val parser2 = JSONParser(json2)
    try {
      parser2.parser()
      fail()
    } catch {
      case e: JSONSyntaxException => //pass test
    }

    val json3 = "[true,fals "
    val parser3 = JSONParser(json3)
    try {
      parser3.parser()
      fail()
    } catch {
      case e: JSONLexerException => //pass test
    }

    val json4 = "[true,f,true] "
    val parser4 = JSONParser(json4)
    try {
      parser4.parser()
      fail()
    } catch {
      case e: JSONLexerException => //pass test
    }
    {
      val json4 = "[true,fa,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[true,fal,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[true,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[t,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[tr,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[tru,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[n,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[nu,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[nul,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[a,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONSyntaxException => //pass test
      }
    }
    {
      val json4 = "[true,false a"
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONSyntaxException => //pass test
      }
    }
    {
      val json4 = "{ \"key1\":true,\"key2\":false a"
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONSyntaxException => //pass test
      }
    }
    {
      val json4 = "[true]"
      val parser4 = JSONParser(json4)
      val value = parser4.parser()
      assert(value === JSONArray(List(true)))
    }
    {
      val json4 = "{\"key\" true}"
      val parser4 = JSONParser(json4)
      try {
        parser4.parser()
        fail()
      } catch {
        case e: JSONSyntaxException => //pass test
      }
    }
    {
      val json4 = "[true,false\r\n\t,true] "
      val parser4 = JSONParser(json4)
      val value = parser4.parser()
      assert(value == JSONArray(List(true, false, true)))
    }
  }

  test("json parser 8") {
    val json =
      """
        |[true false]
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parser()
      fail()
    } catch {
      case e: JSONSyntaxException => //pass test
    }
  }

  test("json parser 9") {
    val json =
      """
        |[]
      """.stripMargin
    val parser = JSONParser(json)
    val value = parser.parser()
    assert(value == JSONArray(List()))

    val json1 =
      """
        |{}
      """.stripMargin
    val parser1 = JSONParser(json1)
    val value1 = parser1.parser()
    assert(value1 == JSONObject(Map()))
  }

  import RuleType._

  test("json pointer parser") {
    var str = "/abcd/0~00/0123[*]/~0~1"
    var jp = JSONPointerParser(str)
    var rules: List[Rule] = jp.parsePath()
    assert(rules == List(Rule("abcd", NORMAL_TOKEN), Rule("0~0", NORMAL_TOKEN), Rule("0123[*]", NORMAL_TOKEN), Rule("~/", NORMAL_TOKEN)))

    str = "/abcd"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("abcd", NORMAL_TOKEN)))

    str = "/0"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(0, NORMAL_TOKEN)))

    str = "/012"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("012", NORMAL_TOKEN)))

    str = "/12"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(12, NORMAL_TOKEN)))

    str = "/"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("", NORMAL_TOKEN)))

    str = "/~0"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("~", NORMAL_TOKEN)))

    str = ""
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List())

    str = "/"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("", NORMAL_TOKEN)))

    str = "/~"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("~", NORMAL_TOKEN)))

    str = "/~1"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("/", NORMAL_TOKEN)))

    str = "/12~0~1"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("12~/", NORMAL_TOKEN)))

    str = "/0~1/"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("0/", NORMAL_TOKEN), Rule("", NORMAL_TOKEN)))

    str = "../../../"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("..", PATH_TOKEN), Rule("..", PATH_TOKEN), Rule("..", PATH_TOKEN), Rule("", NORMAL_TOKEN)))

    str = "./../../"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(".", CURRENT_PATH_TOKEN), Rule("..", NORMAL_TOKEN), Rule("..", NORMAL_TOKEN), Rule("", NORMAL_TOKEN)))

    str = "./"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(".", CURRENT_PATH_TOKEN), Rule("", NORMAL_TOKEN)))

    str = "../"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("..", PATH_TOKEN), Rule("", NORMAL_TOKEN)))

    str = "/0"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(0, NORMAL_TOKEN)))

    str = "//"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("", NORMAL_TOKEN), Rule("", NORMAL_TOKEN)))

    str = "/0/1"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(0, NORMAL_TOKEN), Rule(1, NORMAL_TOKEN)))

    str = "/0/~abc"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(0, NORMAL_TOKEN), Rule("~abc", NORMAL_TOKEN)))

    str = "../."
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("..", PATH_TOKEN), Rule(".", NORMAL_TOKEN)))

    str = "../..abc"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("..", PATH_TOKEN), Rule("..abc", NORMAL_TOKEN)))

    str = "../.abc"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("..", PATH_TOKEN), Rule(".abc", NORMAL_TOKEN)))

    try {
      str = ".../"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException =>
    }

    try {
      str = ".a"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException =>
    }

    try {
      str = "."
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException =>
    }

    try {
      str = ".."
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException =>
    }

    try {
      str = "a"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException =>
    }
  }

  test("json pointer") {
    val json =
      """
        |{
        |  "store": {
        |    "book": [
        |      { "category": {"reference":true},
        |        "author": "Nigel Rees",
        |        "title": "Sayings of the Century",
        |        "price": 8.95
        |      },
        |      { "category": "fiction",
        |        "author": "Evelyn Waugh",
        |        "title": "Sword of Honour",
        |        "price": 12.99
        |      },
        |      { "category": "fiction",
        |        "author": "Herman Melville",
        |        "title": "Moby Dick",
        |        "isbn": "0-553-21311-3",
        |        "price": 8.99
        |      },
        |      { "category": "fiction",
        |        "author": "J. R. R. Tolkien",
        |        "title": "The Lord\r\F\f\n\b\t\\\"\/\u9648\g of the Rings",
        |        "isbn": "0-395-19395-8",
        |        "price": 22.99
        |      }
        |    ],
        |    "bicycle": {
        |      "color": "red",
        |      "price": 19.95
        |    }
        |  }
        |}
      """.stripMargin
    val jp = JSONPointer(json)
    val value = jp.path("/store/book/*/isbn")
    assert(value === List(NotFound, NotFound, "0-553-21311-3", "0-395-19395-8"))

    val value1 = jp.path("/store/book/1-2/isbn")
    assert(value1 === List(NotFound, "0-553-21311-3"))

    val value2 = jp.path("/store/bicycle/color")
    assert(value2 === "red")

    try {
      jp.path("/store/book/3-2/isbn")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

    val value3 = jp.path("/store/book/abc/isbn")
    assert(value3 === NotFound)

    try {
      jp.path("/store/book/1-2-3/isbn")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

    try {
      jp.path("/store/book/1,a,b/isbn")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

    val value4 = jp.path("/store/book/*/category,isbn")
    assert(value4 === List(List(JSONObject(Map("reference" -> true)), NotFound), List("fiction", NotFound), List("fiction", "0-553-21311-3"), List("fiction", "0-395-19395-8")))

    val value5 = jp.path("/store/book/*/*")
    assert(value5 === List(List(JSONObject(Map("reference" -> true)), "Nigel Rees", "Sayings of the Century", 8.95), List("fiction", "Evelyn Waugh", "Sword of Honour", 12.99), List("Herman Melville", 8.99, "0-553-21311-3", "fiction", "Moby Dick"), List("J. R. R. Tolkien", 22.99, "0-395-19395-8", "fiction", "The Lord\r\f\f\n\b\t\\\"/陈\\g of the Rings")))

  }

  test("json pointer 1") {
    val json =
      """
        |    {
        |        "a泉bc": 1.233e10,
        |        "bcd": true,
        |        "c\rde": null
        |    }
      """.stripMargin
    val jp = JSONPointer(json)
    val value = jp.path("/bcd")
    assert(value === true)
  }

  test("json pointer 2") {
    val json =
      """
        |[
        |    155e+012,
        |    2,
        |    "bcd",
        |    [
        |        true,
        |        false,
        |        null
        |    ],
        |    {
        |        "a\u9648bc": 1.233e+10,
        |        "bcd": [true,false],
        |        "c\rde": null,
        |        "0":"object"
        |    },
        |    [
        |        true,
        |        "abc",
        |        null
        |    ],
        |    true,
        |    false,
        |    null
        |]
      """.stripMargin
    val jp = JSONPointer(json)
    val value = jp.path("/4/a陈bc")
    assert(value === 1.233e+10)

    val value1 = jp.path("/*/0")
    assert(value1 === List(NotFound, NotFound, NotFound, true, "object", true, NotFound, NotFound, NotFound))

    val value2 = jp.path("/*/1")
    assert(value2 === List(NotFound, NotFound, NotFound, false, NotFound, "abc", NotFound, NotFound, NotFound))

    val value3 = jp.path("/4/bcd/0")
    assert(value3 === true)

    try {
      jp.path("/a-b/0")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

    try {
      jp.path("/1,/0")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

  }

  test("json pointer reduce") {
    val json =
      """
        |{
        |  "store": {
        |    "book": [
        |      { "category": {"reference":true},
        |        "author": "Nigel Rees",
        |        "title": "Sayings of the Century",
        |        "price": 8.95
        |      },
        |      { "category": "fiction",
        |        "author": "Evelyn Waugh",
        |        "title": "Sword of Honour",
        |        "price": 12.99
        |      },
        |      { "category": "fiction",
        |        "author": "Herman Melville",
        |        "title": "Moby Dick",
        |        "isbn": "0-553-21311-3",
        |        "price": 8.99
        |      },
        |      { "category": "fiction",
        |        "author": "J. R. R. Tolkien",
        |        "title": "The Lord\r\F\f\n\b\t\\\"\/\u9648\g of the Rings",
        |        "isbn": "0-395-19395-8",
        |        "price": 22.99
        |      }
        |    ],
        |    "bicycle": {
        |      "color": "red",
        |      "price": 19.95
        |    }
        |  }
        |}
      """.stripMargin
    val jp = JSONPointer(json)
    val value = jp.reduce(jp.path("/store/book/*/isbn,price"))
    assert(value === List(8.95, 12.99, List("0-553-21311-3", 8.99), List("0-395-19395-8", 22.99)))

    val value1 = jp.reduce(jp.path("/store/book/1-2/isbn"))
    assert(value1 === "0-553-21311-3")

    val value2 = jp.reduce(jp.path("/store/book/1-2/abc"))
    assert(value2 === List())

  }

  test("json pointer path") {
    val json =
      """
        |{
        |  "store": {
        |    "book": [
        |      { "category": {"reference":true},
        |        "author": "Nigel Rees",
        |        "title": "Sayings of the Century",
        |        "price": 8.95
        |      },
        |      { "category": "fiction",
        |        "author": "Evelyn Waugh",
        |        "title": "Sword of Honour",
        |        "price": 12.99
        |      },
        |      { "category": "fiction",
        |        "author": "Herman Melville",
        |        "title": "Moby Dick",
        |        "isbn": "0-553-21311-3",
        |        "price": 8.99
        |      },
        |      { "category": "fiction",
        |        "author": "J. R. R. Tolkien",
        |        "title": "The Lord\r\F\f\n\b\t\\\"\/\u9648\g of the Rings",
        |        "isbn": "0-395-19395-8",
        |        "price": 22.99
        |      }
        |    ],
        |    "bicycle": {
        |      "color": "red",
        |      "price": 19.95,
        |      "..": false
        |    }
        |  }
        |}
      """.stripMargin
    val jp = JSONPointer(json)
    jp.path("/store/book/3/")

    val value1 = jp.path("./isbn")
    assert(value1 === "0-395-19395-8")

    val value2 = jp.path("../2/author")
    assert(value2 === "Herman Melville")

    val value3 = jp.path("../../bicycle/price")
    assert(value3 === 19.95)

    val value4 = jp.path("/store/bicycle/price")
    assert(value4 === 19.95)

    val value5 = jp.path("../color")
    assert(value5 === "red")

    val value6 = jp.path("../~2")
    assert(value6 === false)

    val value7 = jp.path("../..")
    assert(value7 === false)

    {
      val json = "[]"
      val jp = JSONPointer(json)
      val value8 = jp.path("")
      assert(value8 === JSONArray(List.empty))
    }
  }

  test("RFC 6901") {
    val json =
      """
        |{
        |    "foo": ["bar", "baz"],
        |    "": 0,
        |    "a/b": 1,
        |    "c%d": 2,
        |    "e^f": 3,
        |    "g|h": 4,
        |    "i\\j": 5,
        |    "k\"l": 6,
        |    " ": 7,
        |    "m~n": 8,
        |    "0,2":9,
        |    "0-2":10,
        |    "*":11
        |}
      """.stripMargin
    val jp = JSONPointer(json)
    var value = jp.path("")
    assert(value === JSONObject(Map("" -> 0, "*" -> 11, "c%d" -> 2, "i\\j" -> 5, "0,2" -> 9, "m~n" -> 8, "a/b" -> 1, " " -> 7, "g|h" -> 4, "0-2" -> 10, "k\"l" -> 6, "e^f" -> 3, "foo" -> JSONArray(List("bar", "baz")))))

    value = jp.path("/foo")
    assert(value === JSONArray(List("bar","baz")))

    value = jp.path("/foo/0")
    assert(value === "bar")

    value = jp.path("/")
    assert(value === 0)

    value = jp.path("/a~1b")
    assert(value === 1)

    value = jp.path("/c%d")
    assert(value === 2)

    value = jp.path("/e^f")
    assert(value === 3)

    value = jp.path("/g|h")
    assert(value === 4)

    value = jp.path("/i\\j")
    assert(value === 5)

    value = jp.path("/k\"l")
    assert(value === 6)

    value = jp.path("/ ")
    assert(value === 7)

    value = jp.path("/m~0n")
    assert(value === 8)

    value = jp.path("/0,2")
    assert(value === 9)

    value = jp.path("/0-2")
    assert(value === 10)

    value = jp.path("/*")
    assert(value === 11)

  }

}
