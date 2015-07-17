package benchmark

/**
 * Created by leon on 15-7-16.
 */

import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner
import spray.json._

import scala.io.Source

/**
 * Created by leon on 15-6-1.
 */
@RunWith(classOf[JUnitRunner])
class BenchMarkTest extends FunSuite {
  test("parse json speed") {
    val citys: String = Source.fromInputStream(ClassLoader.getSystemResourceAsStream("citys.json")).mkString
    val repos: String = Source.fromInputStream(ClassLoader.getSystemResourceAsStream("repos.json")).mkString
    val request: String = Source.fromInputStream(ClassLoader.getSystemResourceAsStream("request.json")).mkString
    val user: String = Source.fromInputStream(ClassLoader.getSystemResourceAsStream("user.json")).mkString
    val start = System.currentTimeMillis()
    for (i <- 1 to 20) {
      JsonParser(ParserInput(citys))
      JsonParser(ParserInput(repos))
      JsonParser(ParserInput(request))
      JsonParser(ParserInput(user))
    }
    println("0:" + (System.currentTimeMillis() - start))
  }
}
