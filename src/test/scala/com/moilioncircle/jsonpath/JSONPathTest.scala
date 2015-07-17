package com.moilioncircle.jsonpath

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.json._

/**
 * Created by leon on 15-6-1.
 */
@RunWith(classOf[JUnitRunner])
class JSONPathTest extends FunSuite {
  test("json parser 10") {
    val json = "[\"\\u9648\"]"
    val parser = JSONParser(json)
    val value = parser.parse()
    assert(value == JsArray(Vector(JsString("陈"))))

    {
      val json = "[\"\\u964"
      val parser = JSONParser(json)
      try {
        parser.parse()
        fail()
      } catch {
        case e: JSONSyntaxException =>
      }
    }

    {
      val json = ""
      val parser = JSONParser(json)
      try {
        parser.parse()
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
    assert(parser.parse().prettyPrint ===
      """[1.55E+14, 2, 3, [true, false, null], {
        |  "a陈bc": 1.233E-10,
        |  "bcd": true,
        |  "c\rde": null
        |}, true, false, null]""".stripMargin)
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
      parser.parse()
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
      parser.parse()
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
      parser.parse()
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
      parser.parse()
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
      parser.parse()
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
      parser.parse()
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
      parser.parse()
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
      parser1.parse()
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
      parser2.parse()
      fail()
    } catch {
      case e: JSONSyntaxException => //pass test
    }

    val json3 = "[true,fals "
    val parser3 = JSONParser(json3)
    try {
      parser3.parse()
      fail()
    } catch {
      case e: JSONLexerException => //pass test
    }

    val json4 = "[true,f,true] "
    val parser4 = JSONParser(json4)
    try {
      parser4.parse()
      fail()
    } catch {
      case e: JSONLexerException => //pass test
    }
    {
      val json4 = "[true,fa,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[true,fal,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[true,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[t,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[tr,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[tru,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[n,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[nu,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[nul,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONLexerException => //pass test
      }
    }
    {
      val json4 = "[a,fals,true] "
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONSyntaxException => //pass test
      }
    }
    {
      val json4 = "[true,false a"
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONSyntaxException => //pass test
      }
    }
    {
      val json4 = "{ \"key1\":true,\"key2\":false a"
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONSyntaxException => //pass test
      }
    }
    {
      val json4 = "[true]"
      val parser4 = JSONParser(json4)
      val value = parser4.parse()
      assert(value === JsArray(Vector(JsTrue)))
    }
    {
      val json4 = "{\"key\" true}"
      val parser4 = JSONParser(json4)
      try {
        parser4.parse()
        fail()
      } catch {
        case e: JSONSyntaxException => //pass test
      }
    }
    {
      val json4 = "[true,false\r\n\t,true] "
      val parser4 = JSONParser(json4)
      val value = parser4.parse()
      assert(value == JsArray(Vector(JsTrue, JsFalse, JsTrue)))
    }
  }

  test("json parser 8") {
    val json =
      """
        |[true false]
      """.stripMargin
    val parser = JSONParser(json)
    try {
      parser.parse()
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
    val value = parser.parse()
    assert(value == JsArray.empty)

    val json1 =
      """
        |{}
      """.stripMargin
    val parser1 = JSONParser(json1)
    val value1 = parser1.parse()
    assert(value1 == JsObject.empty)
  }

  test("json pointer parser") {
    var str = "/abcd/0~00/0123[*]/~0~1"
    var jp = JSONPointerParser(str)
    var rules: List[Rule] = jp.parsePath()
    assert(rules == List(Rule("abcd"), Rule("0~0"), Rule("0123[*]"), Rule("~/")))

    str = "/abcd"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("abcd")))

    str = "/0"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("0")))

    str = "/012"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("012")))

    str = "/12"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("12")))

    str = "/"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("")))

    str = "/~0"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("~")))

    str = ""
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List())

    str = "/"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("")))

    str = "/~"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("~")))

    str = "/~1"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("/")))

    str = "/12~0~1"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("12~/")))

    str = "/0~1/"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("0/"), Rule("")))

    try {
      str = "../../../"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException => // PASS
    }

    try {
      str = "./../../"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException => // PASS
    }

    try {
      str = "./"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException => // PASS
    }

    try {
      str = "../"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException => // PASS
    }

    str = "/0"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("0")))

    str = "//"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(""), Rule("")))

    str = "/0/1"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("0"), Rule("1")))

    str = "/0/~abc"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("0"), Rule("~abc")))

    try {
      str = "../."
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException => // PASS
    }

    try {
      str = "../..abc"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException => // PASS
    }

    try {
      str = "../.abc"
      jp = JSONPointerParser(str)
      rules = jp.parsePath()
      fail()
    } catch {
      case e: JSONPointerSyntaxException => // PASS
    }

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
    val value = jp.read[List[JsValue]]("/store/book/*/isbn")
    assert(value === Some(List(JsString("0-553-21311-3"), JsString("0-395-19395-8"))))

    val value1 = jp.read[List[JsValue]]("/store/book/1:2/isbn")
    assert(value1 === Some(JsString("0-553-21311-3")))

    val value2 = jp.read[JsString]("/store/bicycle/color")
    assert(value2 === Some(JsString("red")))

    val valuen = jp.read[List[String]]("/store/book/3:2/isbn")
    assert(valuen === Some(List(JsString("0-395-19395-8"), JsString("0-553-21311-3"))))

    try {
      jp.read("/store/book/abc/isbn")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

    try {
      jp.read("/store/book/1:2:3/isbn")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

    try {
      jp.read("/store/book/1,a,b/isbn")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

    val value4 = jp.read[List[JsValue]]("/store/book/*/category,isbn")
    assert(value4 === Some(List(JsObject(Map("reference" -> JsTrue)), JsString("fiction"), List(JsString("fiction"), JsString("0-553-21311-3")), List(JsString("fiction"), JsString("0-395-19395-8")))))

    val value5 = jp.read[List[Any]]("/store/book/*/*")
    assert(value5 === Some(List(List(JsObject(Map("reference" -> JsTrue)), JsString("Nigel Rees"), JsString("Sayings of the Century"), JsNumber(8.95)), List(JsString("fiction"), JsString("Evelyn Waugh"), JsString("Sword of Honour"), JsNumber(12.99)), List(JsString("Herman Melville"), JsNumber(8.99), JsString("0-553-21311-3"), JsString("fiction"), JsString("Moby Dick")), List(JsString("J. R. R. Tolkien"), JsNumber(22.99), JsString("0-395-19395-8"), JsString("fiction"), JsString("The Lord\r\f\f\n\b\t\\\"/陈\\g of the Rings")))))

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
    val value = jp.read[JsBoolean]("/bcd")
    assert(value === Some(JsTrue))
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
    val value = jp.read[JsNumber]("/4/a陈bc")
    assert(value === Some(JsNumber(1.233e+10)))

    val value1 = jp.read[List[JsValue]]("/*/0")
    assert(value1 === Some(List(JsTrue, JsString("object"), JsTrue)))

    val value2 = jp.read[List[JsValue]]("/*/1")
    assert(value2 === Some(List(JsFalse, JsString("abc"))))

    val value3 = jp.read[JsBoolean]("/4/bcd/0")
    assert(value3 === Some(JsTrue))

    try {
      jp.read("/a:b/0")
      fail()
    } catch {
      case e: JSONPointerException =>
    }

    try {
      jp.read("/1,/0")
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
    val value = jp.read[List[_]]("/store/book/*/isbn,price")
    assert(value === Some(List(JsNumber(8.95), JsNumber(12.99), List(JsString("0-553-21311-3"), JsNumber(8.99)), List(JsString("0-395-19395-8"), JsNumber(22.99)))))

    val value1 = jp.read[JsString]("/store/book/1:2/isbn")
    assert(value1 === Some(JsString("0-553-21311-3")))

    val value2 = jp.read[Any]("/store/book/1:2/abc")
    assert(value2 === None)

  }

  test("json pointer read") {
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
    jp.read("/store/book/3")

    val value4 = jp.read[JsNumber]("/store/bicycle/price")
    assert(value4 === Some(JsNumber(19.95)))

    {
      val json = "[]"
      val jp = JSONPointer(json)
      val value8 = jp.read[JsArray]("")
      assert(value8 === Some(JsArray.empty))
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
    var value: Any = jp.read[JsObject]("")
    assert(value === Some(JsObject(Map("" -> JsNumber(0), "*" -> JsNumber(11), "c%d" -> JsNumber(2), "i\\j" -> JsNumber(5), "0,2" -> JsNumber(9), "m~n" -> JsNumber(8), "a/b" -> JsNumber(1), " " -> JsNumber(7), "g|h" -> JsNumber(4), "0-2" -> JsNumber(10), "k\"l" -> JsNumber(6), "e^f" -> JsNumber(3), "foo" -> JsArray(Vector(JsString("bar"), JsString("baz")))))))

    value = jp.read[JsArray]("/foo")
    assert(value === Some(JsArray(Vector(JsString("bar"), JsString("baz")))))

    value = jp.read[JsString]("/foo/0")
    assert(value === Some(JsString("bar")))

    value = jp.read[JsNumber]("/")
    assert(value === Some(JsNumber(0)))

    value = jp.read[JsNumber]("/a~1b")
    assert(value === Some(JsNumber(1)))

    value = jp.read[JsNumber]("/c%d")
    assert(value === Some(JsNumber(2)))

    value = jp.read[JsNumber]("/e^f")
    assert(value === Some(JsNumber(3)))

    value = jp.read[JsNumber]("/g|h")
    assert(value === Some(JsNumber(4)))

    value = jp.read[JsNumber]("/i\\j")
    assert(value === Some(JsNumber(5)))

    value = jp.read[JsNumber]("/k\"l")
    assert(value === Some(JsNumber(6)))

    value = jp.read[JsNumber]("/ ")
    assert(value === Some(JsNumber(7)))

    value = jp.read[JsNumber]("/m~0n")
    assert(value === Some(JsNumber(8)))

    value = jp.read[JsNumber]("/0~,2")
    assert(value === Some(JsNumber(9)))

    value = jp.read[JsNumber]("/0-2")
    assert(value === Some(JsNumber(10)))

    value = jp.read[JsNumber]("/~*")
    assert(value === Some(JsNumber(11)))

  }

  test("json pointer neg number") {
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

    {
      val jp = JSONPointer(json)
      val value = jp.read[JsBoolean]("/-2")
      assert(value === Some(JsFalse))
    }
    {
      val jp = JSONPointer(json)
      val value = jp.read[List[JsValue]]("/-2,-1")
      assert(value === Some(List(JsFalse, JsNull)))
    }
    {
      val jp = JSONPointer(json)
      val value = jp.read[List[JsValue]]("/:")
      assert(value === Some(List(JsNumber(1.55E14), JsNumber(0.55), JsNumber(0), JsNumber(9), JsNumber(100), JsNumber(1.1E14), JsNumber(1.1E14), JsNumber(1.1E-10), JsNumber(3), JsNumber(-100), JsArray(Vector(JsTrue, JsFalse, JsNull)), JsObject(Map("a泉bc" -> JsNumber(1.233E-10), "bcd" -> JsTrue, "c\rde" -> JsNull)), JsTrue, JsFalse, JsNull)))
    }
    {
      try {
        val jp = JSONPointer(json)
        jp.read("/9a")
        fail()
      } catch {
        case e: JSONPointerException => // PASS
      }
    }
    {
      try {
        val jp = JSONPointer(json)
        jp.read("/9a,9b,")
        fail()
      } catch {
        case e: JSONPointerException => // PASS
      }
    }
    {
      try {
        val jp = JSONPointer(json)
        jp.read("/")
        fail()
      } catch {
        case e: JSONPointerException => // PASS
      }
    }
    {
      try {
        val jp = JSONPointer(json)
        jp.read("/-")
        fail()
      } catch {
        case e: JSONPointerException => // PASS
      }
    }
    {
      try {
        val jp = JSONPointer(json)
        jp.read("/-01")
        fail()
      } catch {
        case e: JSONPointerException => // PASS
      }
    }
    {
      try {
        val jp = JSONPointer(json)
        jp.read("/100")
        fail()
      } catch {
        case e: JSONPointerException => // PASS
      }
    }
    {
      try {
        val jp = JSONPointer(json)
        jp.read("/ , , ")
        fail()
      } catch {
        case e: JSONPointerException => // PASS
      }
    }
    {
      val jp = JSONPointer(json)
      val value = jp.read[List[JsValue]]("/-1,5,-2")
      assert(value === Some(List(JsNull, JsNumber(1.1E14), JsFalse)))
    }
    {
      val value = JSONPointer().read[JsNumber]("/0", json)
      assert(value === Some(JsNumber(155e+012)))
    }
    {
      val jsonObj = JSONParser(json).parse()
      assert(JSONPointer().read[JsNumber]("/0", jsonObj) == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer().read[JsNumber]("/0", json.iterator) == Some(JsNumber(155e+012)))
    }
    {
      val jsonObj = JSONParser(json).parse()
      assert(JSONPointer().read[JsNumber]("/0", jsonObj) == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer().read[JsNumber]("/0", json.iterator) == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer().read[JsNumber]("/0", json) == Some(JsNumber(155e+012)))
    }
    {
      try {
        JSONPointer().read("/0")
        fail()
      } catch {
        case e: IllegalArgumentException =>
      }
    }
    {
      try {
        JSONPointer().read("../", JSONParser(json).parse())
        fail()
      } catch {
        case e: JSONPointerSyntaxException =>
      }
    }
    {
      val value = JSONPointer().read[JsNumber](new Path / "0", json)
      assert(value === Some(JsNumber(155e+012)))
    }
    {
      val jsonObj = JSONParser(json).parse()
      assert(JSONPointer().read[JsNumber](new Path / "0", jsonObj) == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer().read[JsNumber](new Path / "0", json.iterator) == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer(json).read[JsNumber](new Path / "0") == Some(JsNumber(155e+012)))
    }
    {
      val jsonObj = JSONParser(json).parse()
      assert(JSONPointer().read[JsNumber](new Path / "0", jsonObj) == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer().read[JsNumber](new Path / "0", json.iterator) == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer().read[JsNumber](new Path / "0", json) == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer(json).read[JsNumber](new Path / "0") == Some(JsNumber(155e+012)))
    }
    {
      assert(JSONPointer(json).read[JsValue](new Path / -1) == Some(JsNull))
    }
  }
}
