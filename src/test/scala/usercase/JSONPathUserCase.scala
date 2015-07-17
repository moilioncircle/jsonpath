package usercase

import com.moilioncircle.jsonpath._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.json._

/**
 * Created by leon on 15-6-24.
 */
@RunWith(classOf[JUnitRunner])
class JSONPathUserCase extends FunSuite {
  test("user case 1") {
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

    val value = JSONPointer().read[List[Any]](new Path / -3 /("bcd", ""), json)
    assert(value === Some(List(JsTrue, JsNumber(1.233E-10))))

    val value1 = JSONPointer().read[List[Any]](new Path / * /(*, (e: String) => e.contains("b")), json)
    assert(value1 === Some(List(List(JsNumber(1.233E-10), JsTrue, JsNull), List(JsTrue, JsNumber(1.23)))))

    val value2 = JSONPointer().read[Any](new Path / (1 -> -1) /(*, (_: String) == "b"), json)
    assert(value2 === Some(List(JsNull, JsNumber(1.23))))

    val value3 = JSONPointer().read[List[Any]](new Path /(*, (e: Int, size: Int) => e > 5 && e < size), json).get.filterNot(_ match {
      case e: JsArray => true
      case e: JsObject => true
      case _ => false
    })
    assert(value3 === List(JsNumber(1.1E14), JsNumber(1.1E-10), JsNumber(3), JsNumber(-100), JsBoolean(false), JsNull))

    val value4 = JSONPointer().read[Any](new Path / -1, json)
    assert(value4 === Some(JsNull))

    val value5 = JSONPointer().read[List[Any]](new Path /(*, _ < _ - 8), json)
    assert(value5 === Some(List(JsNumber(1.55E14), JsNumber(0.55), JsNumber(0), JsNumber(9), JsNumber(100), JsNumber(1.1E14), JsNumber(1.1E14))))

    val value6 = JSONPointer().read[List[Any]]("/*/*", json, List(None, Some((e: String) => e.contains("b"))))
    assert(value6 === Some(List(List(JsNumber(1.233E-10), JsTrue, JsNull), List(JsTrue, JsNumber(1.23)))))

    val value7 = JSONPointer().read[Any]("/-3/1", json)
    assert(value7 === None)
  }
}

