/*
 * Copyright 2015 leon chen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.moilioncircle.jsonpath

/**
 *
 * An implementation of RFC6901.
 * Provide a convenient way to access json notation data using the path which like file path.
 *
 * ===RFC6901 example===
 * @example
 * {{{// For example, given the JSON document
 * {
 * "foo": ["bar", "baz"],
 * "": 0,
 * "a/b": 1,
 * "c%d": 2,
 * "e&#94;f": 3,
 * "g|h": 4,
 * "i\\j": 5,
 * "k\"l": 6,
 * " ": 7,
 * "m~n": 8
 * }
 *
 * // The following JSON strings evaluate to the accompanying values:
 *
 * ""           // the whole document
 * "/foo"       ["bar", "baz"]
 * "/foo/0"     "bar"
 * "/"          0
 * "/a~1b"      1
 * "/c%d"       2
 * "/e&#94;f"       3
 * "/g|h"       4
 * "/i\\j"      5
 * "/k\"l"      6
 * "/ "         7
 * "/m~0n"      8
 * }}}
 *
 * ===usage===
 * {{{val json =
 * """
 * |{
 * |  "store": {
 * |    "book": [
 * |      { "category": "reference",
 * |        "author": "Nigel Rees",
 * |        "title": "Sayings of the Century",
 * |        "price": 8.95
 * |      },
 * |      { "category": "fiction",
 * |        "author": "Evelyn Waugh",
 * |        "title": "Sword of Honour",
 * |        "price": 12.99
 * |      },
 * |      { "category": "fiction",
 * |        "author": "Herman Melville",
 * |        "title": "Moby Dick",
 * |        "isbn": "0-553-21311-3",
 * |        "price": 8.99
 * |      },
 * |      { "category": "fiction",
 * |        "author": "J. R. R. Tolkien",
 * |        "title": "The Lord of the Rings",
 * |        "isbn": "0-395-19395-8",
 * |        "price": 22.99
 * |      }
 * |    ],
 * |    "bicycle": {
 * |      "color": "red",
 * |      "price": 19.95
 * |    }
 * |  }
 * |}
 * """.stripMargin
 * val jp = JSONPointer(json)
 * val value = jp.read[String]("/store/book/0/isbn")
 * }}}
 *
 * @author leon.chen
 * @version 0.4.0
 * @since   1.0.0
 * @see [[http://tools.ietf.org/html/rfc6901 "JSON Pointer (RFC 6901)"]]
 * @see [[http://json.org "JSON (JavaScript Object Notation)"]]
 *
 */

import com.moilioncircle.jsonpath.RuleType.RuleType
import spray.json._

/**
 * scala DSL Wildcard
 */
case class *()

/**
 * scala DSL Path
 */
class Path() {

  private[moilioncircle] type Wildcard = () => *

  import Path._

  private[moilioncircle] var path: List[Rule] = List.empty[Rule]

  /**
   * @param args one or multiply JSONObject keys.{{{new Path / "foo"}}}{{{new Path /("foo","bar")}}}
   * @return scala DSL Path.
   */
  def /(args: String*): Path = {
    if (args.size > 1) {
      path = path :+ Rule(args.mkString(","), RuleType.SPLIT, None, args.toList)
    } else {
      path = path :+ Rule(args.mkString(","))
    }
    this
  }

  /**
   *
   * @param args one or more JSONArray indices.{{{new Path / 1}}}{{{new Path /(1,-1,5)}}}
   * @param d **HACK** to avoid scala multiply overload method error.
   * @return scala DSL Path.
   */
  def /(args: Int*)(implicit d: DummyImplicit): Path = {
    if (args.size > 1) {
      path = path :+ Rule(args.mkString(","), RuleType.SPLIT, None, args.map(_.toString).toList)
    } else {
      path = path :+ Rule(args.mkString(","))
    }
    this
  }

  /**
   * @param arg JSONArray slice.{{{new Path / (1->5)}}}
   * @return scala DSL Path.
   */
  def /(arg: (Int, Int)): Path = {
    path = path :+ Rule(arg._1 + ":" + arg._2)
    this
  }

  /**
   * @param wildcard match all JSONArray or JSONObject.{{{new Path / *}}}
   * @return scala DSL Path.
   */
  def /(wildcard: Wildcard): Path = {
    path = path :+ Rule("*", RuleType.WILDCARD)
    this
  }

  /**
   * @param wildcard match all JSONArray or JSONObject.{{{new Path / (*,(key:String)=>key.contains("foo"))}}}
   * @param f filter JSONObject via key.
   * @return scala DSL Path.
   */
  def /(wildcard: Wildcard, f: String => Boolean): Path = {
    path = path :+ Rule("*", RuleType.WILDCARD, Some(f))
    this
  }

  /**
   * @param wildcard match all JSONArray or JSONArray.{{{new Path / (*,(index:Int)=>index == 5)}}}
   * @param f filter JSONObject via index.
   * @param d **HACK** to avoid scala multiply overload method error.
   * @return scala DSL Path.
   */
  def /(wildcard: => Wildcard, f: Int => Boolean)(implicit d: DummyImplicit): Path = {
    path = path :+ Rule("*", RuleType.WILDCARD, Some(f))
    this
  }

  /**
   * @param wildcard match all JSONArray or JSONArray.{{{new Path / (*,(index:Int,length:Int)=>index < length)}}}
   * @param f filter JSONObject via index,second param represents JSONArray length.
   * @param d **HACK** to avoid scala multiply overload method error.
   * @return scala DSL Path.
   */
  def /(wildcard: => Wildcard, f: (Int, Int) => Boolean)(implicit d: DummyImplicit): Path = {
    path = path :+ Rule("*", RuleType.WILDCARD, Some(f))
    this
  }

  /**
   * @param that add that path to this path
   * @return scala DSL Path.
   */
  def +(that: Path): Path = {
    path = path ::: that.path
    this
  }

  private[moilioncircle] def build(): List[Rule] = {
    val rs = path
    path = List.empty[Rule]
    rs
  }

  /**
   * @return path string used by DEBUG
   */
  override def toString(): String = {
    path.map(e => if (e.ruleType == RuleType.TOKEN) quote(e.rule) else e.rule).mkString("/", "/", "")
  }
}

/**
 * NotFound
 */
private object NotFound

/**
 * help method used by string path parser.
 */
object Path {

  /**
   * @param str unquote str {{{quote("foo,bar")}}}
   * @return quoted str {{{"foo~,bar"}}}
   */
  def quote(str: String): String = {
    val it: Iterator[Char] = str.iterator
    val sb: StringBuilder = new StringBuilder
    while (it.hasNext) {
      it.next() match {
        case '~' =>
          sb.append('~')
          sb.append('0')
        case '/' =>
          sb.append('~')
          sb.append('1')
        case ',' =>
          sb.append('~')
          sb.append(',')
        case '*' =>
          sb.append('~')
          sb.append('*')
        case ch => sb.append(ch)
      }
    }
    sb.toString()
  }
}

/**
 * JSON Pointer implementation
 */
object JSONPointer {
  def apply(json: String): JSONPointer = new JSONPointer(Some(JSONParser(json).parse()))

  def apply(json: Iterator[Char]): JSONPointer = new JSONPointer(Some(JSONParser(json).parse()))

  def apply(): JSONPointer = new JSONPointer(None)
}

private[moilioncircle] case class Rule(rule: String, ruleType: RuleType = RuleType.TOKEN, filter: Option[Any] = None, splits: List[String] = List.empty[String])

private[moilioncircle] object RuleType extends Enumeration {
  type RuleType = Value
  val TOKEN, WILDCARD, SPLIT = Value
}

private[moilioncircle] object JSONPointerParser {
  def apply(json: String): JSONPointerParser = new JSONPointerParser(json.iterator)
}

private[moilioncircle] class JSONPointerParser(it: Iterator[Char]) {

  private var backBuffer = List.empty[Char]

  def parsePath(): List[Rule] = {
    if (!hasNext()) {
      List.empty[Rule]
    } else {
      next() match {
        case '/' =>
          back('/')
          var rules = List.empty[Rule]
          while (hasNext() && next() == '/') {
            val rule = parseRule()
            rules = rules :+ rule
          }
          rules
        case e => throw JSONPointerSyntaxException(s"excepted '/' but '$e'")
      }
    }
  }

  private def parseRule(): Rule = {
    if (!hasNext()) {
      Rule("")
    } else {
      var ch = next()
      if (ch == '/') {
        back('/')
        Rule("")
      } else if (ch == '*') {
        if (hasNext()) {
          ch = next()
          if (ch == '/') {
            back('/')
            Rule("*", RuleType.WILDCARD)
          } else {
            back(ch)
            parseString('*')
          }
        } else {
          Rule("*", RuleType.WILDCARD)
        }
      } else {
        parseString(ch)
      }
    }
  }

  private def parseString(ch: Char): Rule = {
    var splits: List[String] = List.empty[String]
    var sb: StringBuilder = new StringBuilder
    back(ch)
    var c: Char = 0
    while (hasNext() && {
      c = next()
      c != '/'
    }) {
      if (c == '~') {
        if (hasNext()) {
          c = next()
          if (c == '0') {
            sb.append('~')
          } else if (c == '1') {
            sb.append('/')
          } else if (c == '*') {
            sb.append('*')
          } else if (c == ',') {
            sb.append(',')
          } else {
            sb.append('~')
            sb.append(c)
          }
        } else {
          sb.append('~')
        }
      } else if (c == ',') {
        splits = splits :+ sb.toString
        sb = new StringBuilder
      } else {
        sb.append(c)
      }
    }
    if (!hasNext()) {
    }
    if (c == '/') {
      back(c)
    }
    if (splits.nonEmpty) {
      splits = splits :+ sb.toString
      Rule(splits.mkString(","), RuleType.SPLIT, None, splits)
    } else {
      Rule(sb.toString)
    }
  }

  private def hasNext(): Boolean = {
    backBuffer.nonEmpty || it.hasNext
  }

  private def next(): Char = {
    if (backBuffer.nonEmpty) {
      val c = backBuffer.head
      backBuffer = backBuffer.tail
      c
    } else {
      it.next()
    }
  }

  private def back(char: Char): Unit = {
    backBuffer = backBuffer :+ char
  }
}

private[moilioncircle] class JSONPointer(json: Option[JsValue]) {
  private type ArrayFilter = Int => Boolean
  private type ObjectFilter = String => Boolean

  def read[T](path: String): Option[T] = {
    require(json != None, "json is None")
    read[T](path, json.get, List.empty)
  }

  def read[T](path: String, json: String): Option[T] = {
    read[T](path, JSONParser(json).parse(), List.empty)
  }

  def read[T](path: String, json: Iterator[Char]): Option[T] = {
    read[T](path, JSONParser(json).parse(), List.empty)
  }

  def read[T](path: String, json: JsValue): Option[T] = {
    val rules = JSONPointerParser(path).parsePath()
    read[T](mergeFilter(rules, List.empty), json)
  }

  def read[T](path: String, filters: List[Option[Any]]): Option[T] = {
    require(json != None, "json is None")
    read[T](path, json.get, filters)
  }

  def read[T](path: String, json: String, filters: List[Option[Any]]): Option[T] = {
    read[T](path, JSONParser(json).parse(), filters)
  }

  def read[T](path: String, json: Iterator[Char], filters: List[Option[Any]]): Option[T] = {
    read[T](path, JSONParser(json).parse(), filters)
  }

  def read[T](path: String, json: JsValue, filters: List[Option[Any]]): Option[T] = {
    val rules = JSONPointerParser(path).parsePath()
    read[T](mergeFilter(rules, filters), json)
  }

  def read[T](path: Path): Option[T] = {
    read[T](path.build)
  }

  def read[T](path: Path, json: String): Option[T] = {
    read[T](path.build, json)
  }

  def read[T](path: Path, json: Iterator[Char]): Option[T] = {
    read[T](path.build, json)
  }

  def read[T](path: Path, json: JsValue): Option[T] = {
    read[T](path.build, json)
  }

  private def read[T](path: List[Rule]): Option[T] = {
    require(json != None, "json is None")
    read[T](path, json.get)
  }

  private def read[T](path: List[Rule], json: String): Option[T] = {
    read[T](path, JSONParser(json).parse())
  }

  private def read[T](path: List[Rule], json: Iterator[Char]): Option[T] = {
    read[T](path, JSONParser(json).parse())
  }

  private def read[T](rules: List[Rule], json: JsValue): Option[T] = {
    require(json match {
      case JsObject(fields) => true
      case JsArray(elements) => true
      case _ => false
    }, "need JsObject or JsArray")
    var temp: Any = json
    rules.foreach(rule => temp = solver(rule, temp))
    reduce(temp).asInstanceOf[T] match {
      case NotFound => None
      case obj => Some(obj)
    }
  }

  private def mergeFilter(rules: List[Rule], list: List[Option[Any]]): List[Rule] = {
    var temp = list
    if (list.isEmpty) {
      rules
    } else {
      rules.map(r => {
        if (r.ruleType == RuleType.WILDCARD) {
          val v = r.copy(filter = temp.head)
          temp = temp.tail
          v
        } else {
          r
        }
      })
    }
  }

  private def reduce(obj: Any): Any = {
    obj match {
      case list: List[_] =>
        val result = list.filter(e => e != NotFound && e != List.empty).map(reduce(_))
        result.size match {
          case 1 => result(0)
          case 0 => NotFound
          case _ => result
        }
      case obj => obj
    }
  }

  private def solver(rule: Rule, temp: Any): Any = {
    temp match {
      case jsonAry: JsArray =>
        val arySize = jsonAry.elements.size
        rule.ruleType match {
          case RuleType.TOKEN =>
            val str = rule.rule
            if (str.indexOf(':') >= 0) {
              val from: Int = parseArrayIndex(str.substring(0, str.indexOf(":")), arySize, 0)
              val until = parseArrayIndex(str.substring(str.indexOf(":") + 1), arySize, arySize - 1)
              if (from > until) jsonAry.elements.slice(until, from + 1).reverse.toList else jsonAry.elements.slice(from, until + 1).toList
            } else {
              jsonAry.elements(parseArrayIndex(str, arySize, throw JSONPointerException(s"excepted a number but '$str'")))
            }
          case RuleType.SPLIT =>
            rule.splits.map(e => {
              jsonAry.elements(parseArrayIndex(e, arySize, throw JSONPointerException(s"excepted a number but '$e'")))
            })
          case RuleType.WILDCARD =>
            rule.filter match {
              case Some(f) =>
                try {
                  (0 until arySize).filter(f.asInstanceOf[Int => Boolean](_)).map(jsonAry.elements(_)).toList
                } catch {
                  case e: ClassCastException =>
                    try {
                      (0 until arySize).filter(f.asInstanceOf[(Int, Int) => Boolean](_, arySize)).map(jsonAry.elements(_)).toList
                    } catch {
                      case e: ClassCastException => NotFound
                    }
                }
              case None => jsonAry.elements.toList
              case _ => NotFound
            }
        }
      case jsonObj: JsObject =>
        rule.ruleType match {
          case RuleType.TOKEN =>
            val str = rule.rule
            jsonObj.fields.getOrElse(str, NotFound)
          case RuleType.SPLIT =>
            rule.splits.map(jsonObj.fields.getOrElse(_, NotFound))
          case RuleType.WILDCARD =>
            rule.filter match {
              case Some(f) =>
                try {
                  jsonObj.fields.keys.filter(f.asInstanceOf[String => Boolean](_)).map(jsonObj.fields(_)).toList
                } catch {
                  case e: ClassCastException => NotFound
                }
              case None => jsonObj.fields.values.toList
              case _ => NotFound
            }
        }
      case list: List[_] => list.map(solver(rule, _))
      case _ => NotFound
    }
  }

  private def parseArrayIndex(str: String, size: Int, default: => Int): Int = {
    val it: Iterator[Char] = str.trim.iterator
    def parseDigit(c: Char, sb: StringBuilder): Boolean = {
      c match {
        case c if c >= '0' && c <= '9' =>
          sb.append(c)
          true
        case e =>
          false
      }
    }

    def nextChar(): Char = {
      it.next()
    }

    def hasNext(): Boolean = {
      it.hasNext
    }

    def str2int(str: String, size: Int): Int = {
      val value = str.toInt match {
        case v if v < 0 => size + v
        case v => v
      }
      if (value < 0 || value >= size) {
        throw JSONPointerException(s"out of range,array size is $size")
      } else {
        value
      }
    }

    if (!hasNext) {
      default
    } else {
      val sb = new StringBuilder
      var next = nextChar()
      if (next == '-') {
        sb.append('-')
        if (hasNext) {
          next = nextChar()
        } else {
          throw JSONPointerException(s"excepted a number but '$str'")
        }
      }
      next match {
        case '0' =>
          sb.append('0')
          if (!hasNext) {
            0
          } else {
            throw JSONPointerException(s"excepted a number but '$str'")
          }

        case ch if ch > '0' && ch <= '9' =>
          sb.append(next)
          if (hasNext) {
            next = nextChar()
            while (parseDigit(next, sb)) {
              if (hasNext) {
                next = nextChar()
              } else {
                return str2int(sb.toString(), size)
              }
            }
            if (next < '0' || next > '9') {
              throw JSONPointerException(s"excepted a number but '$str'")
            }
          }
          str2int(sb.toString(), size)

        case _ => throw JSONPointerException(s"excepted a number but '$str'")
      }
    }
  }
}
