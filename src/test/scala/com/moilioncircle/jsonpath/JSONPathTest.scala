package com.moilioncircle.jsonpath

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

/**
 * Created by leon on 15-6-1.
 */
@RunWith(classOf[JUnitRunner])
class JSONPathTest extends FunSuite {
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
    parser.parser() match {
      case JSONArray(list: List[Any]) => assert(list.toString === "List(1.55E14, 2, 3, JSONArray(List(true, false, null)), JSONObject(Map(a泉bc -> 1.233E-10, bcd -> true, c\rde -> null)), true, false, null)")
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
    assert(rules == List(Rule(0)))

    str = "/012"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("012")))

    str = "/12"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(12)))

    str = "/"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("")))

    str = "/~0"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("~")))

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

    str = "../../../"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(".."), Rule(".."), Rule(".."), Rule("")))

    str = "./../../"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("."), Rule(".."), Rule(".."), Rule("")))

    str = "./"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule("."), Rule("")))

    str = "../"
    jp = JSONPointerParser(str)
    rules = jp.parsePath()
    assert(rules == List(Rule(".."), Rule("")))
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
        |        "title": "The Lord\r\F\f\n\b\t\\g of the Rings",
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
    assert(value5 === List(List(JSONObject(Map("reference" -> true)), "Nigel Rees", "Sayings of the Century", 8.95), List("fiction", "Evelyn Waugh", "Sword of Honour", 12.99), List("Herman Melville", 8.99, "0-553-21311-3", "fiction", "Moby Dick"), List("J. R. R. Tolkien", 22.99, "0-395-19395-8", "fiction", "The Lord\r\f\f\n\b\t\\g of the Rings")))

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
        |        "bcd": true,
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
  }
}
