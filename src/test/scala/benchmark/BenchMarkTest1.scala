package benchmark

/**
 * Created by leon on 15-7-16.
 */

import com.moilioncircle.jsonpath.JSONParser
import org.junit.runner.RunWith
import org.scalatest.FunSuite
import org.scalatest.junit.JUnitRunner

import scala.io.Source

/**
 * Created by leon on 15-6-1.
 */
@RunWith(classOf[JUnitRunner])
class BenchMarkTest1 extends FunSuite {
  test("parse json speed1") {
    val citys: String = Source.fromInputStream(ClassLoader.getSystemResourceAsStream("citys.json")).mkString
    val repos: String = Source.fromInputStream(ClassLoader.getSystemResourceAsStream("repos.json")).mkString
    val request: String = Source.fromInputStream(ClassLoader.getSystemResourceAsStream("request.json")).mkString
    val user: String = Source.fromInputStream(ClassLoader.getSystemResourceAsStream("user.json")).mkString
    val start = System.currentTimeMillis()
    for (i <- 1 to 20) {
      JSONParser(citys).parser()
      JSONParser(repos).parser()
      JSONParser(request).parser()
      JSONParser(user).parser()
    }
    println("1:" + (System.currentTimeMillis() - start))
  }
}
