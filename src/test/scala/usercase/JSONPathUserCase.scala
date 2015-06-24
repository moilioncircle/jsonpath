package usercase

import com.moilioncircle.jsonpath._
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

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
    assert(value === List(true, 1.233E-10))

    val value1 = JSONPointer().reduceRead[List[Any]](new Path / * /(*, (e: String) => e.contains("b")), json)
    assert(value1 === List(List(1.233E-10, true, null), List(true, 1.23)))

    val value2 = JSONPointer().reduceRead[Any](new Path / (1 -> -1) /(*, (_: String) == "b"), json)
    assert(value2 === List(null, 1.23))

    val value3 = JSONPointer().reduceRead[List[Any]](new Path /(*, (e: Int, size: Int) => e > 5 && e < size), json).filterNot(_ match {
      case e: JSONArray => true
      case e: JSONObject => true
      case _ => false
    })
    assert(value3 === List(1.1E14, 1.1E-10, 3, -100, false, null))

    val value4 = JSONPointer().reduceRead[Any](new Path / -1, json)
    assert(value4 === null)

    val value5 = JSONPointer().reduceRead[List[Any]](new Path /(*, _ < _ - 8), json)
    assert(value5 === List(1.55E14, 0.55, 0, 9, 100, 1.1E14, 1.1E14))

    val value6 = JSONPointer().reduceRead[List[Any]]("/*/*", json, List(None, Some((e: String) => e.contains("b"))))
    assert(value6 === List(List(1.233E-10, true, null), List(true, 1.23)))

    val value7 = JSONPointer().reduceRead[Any]("/-3/1", json)
    assert(value7 === NotFound)

  }
}
