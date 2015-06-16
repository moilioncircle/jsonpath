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

import com.moilioncircle.jsonpath.RuleType.RuleType

/**
 *
 * An implementation of RFC6901.
 * Provide a convenient way to access json object using the path which like file path.
 *
 * ===RFC6901 example===
 * @example
 * {{{
 *                                                                                                                                 // For example, given the JSON document
 * {
 * "foo": ["bar", "baz"],
 * "": 0,
 * "a/b": 1,
 * "c%d": 2,
 * "e^f": 3,
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
 * "/e^f"       3
 * "/g|h"       4
 * "/i\\j"      5
 * "/k\"l"      6
 * "/ "         7
 * "/m~0n"      8
 * }}}
 *
 * ===usage===
 * {{{
 *                                                                                                                                   val json =
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
 * val value = jp.path("/store/book/0-2/isbn")
 * }}}
 *
 * @author leon.chen
 * @version 0.0.1
 * @since   1.0.0
 * @see [[http://tools.ietf.org/html/rfc6901 "JSON Pointer (RFC 6901)"]]
 * @see [[http://json.org "JSON (JavaScript Object Notation)"]]
 *
 */
abstract class JSONType

case class JSONObject(map: Map[String, Any]) extends JSONType

case class JSONArray(list: List[Any]) extends JSONType

sealed case class Rule(rule: Any, ruleType: RuleType)

case object NotFound

object JSONPointerParser {
  def apply(str: String): JSONPointerParser = new JSONPointerParser(str)
}

object JSONParser {
  def apply(json: String): JSONParser = new JSONParser(json)
}

object JSONPointer {
  def apply(json: String): JSONPointer = new JSONPointer(json)
}

class JSONParser(json: String) {
  private val it: Iterator[Char] = json.iterator
  private var column: Int = 0
  private var row: Int = 0
  private var backBuffer = List.empty[Char]
  private var backCharPosition = List.empty[(Int, Int)]

  def parser(): JSONType = {
    next() match {
      case '{' => parseObject()
      case '[' => parseArray()
      case e => throw JSONSyntaxException(s"excepted ['[' , '{'] but '$e' at row $row,column $column")
    }
  }

  private def parseObject(): JSONObject = {
    var map = Map.empty[String, Any]
    next() match {
      case '}' => JSONObject(map)
      case c =>
        back(c)
        map += parseItem()
        next() match {
          case ',' =>
            back(',')
            var ch = next()
            while (ch == ',') {
              map += parseItem()
              ch = next()
            }
            ch match {
              case '}' =>
                JSONObject(map)
              case e => throw JSONSyntaxException(s"excepted '}' but '$e' at row $row,column $column")
            }
          case '}' =>
            JSONObject(map)
          case e => throw JSONSyntaxException(s"excepted [',' , '}'] but '$e' at row $row,column $column")
        }
    }
  }

  private def parseArray(): JSONArray = {
    var list = List.empty[Any]
    next() match {
      case ']' =>
        JSONArray(list)
      case ch =>
        back(ch)
        list = list :+ parseValue()
        next() match {
          case ',' =>
            back(',')
            var ch = next()
            while (ch == ',') {
              list = list :+ parseValue()
              ch = next()
            }
            ch match {
              case ']' =>
                JSONArray(list)
              case e => throw JSONSyntaxException(s"excepted ']' but '$e' at row $row,column $column")
            }
          case ']' =>
            JSONArray(list)
          case e => throw JSONSyntaxException(s"excepted [',' , ']'] but '$e' at row $row,column $column")
        }
    }
  }

  private def parseValue(): Any = {
    next() match {
      case '"' =>
        back('"')
        parseString()
      case 't' =>
        back('t')
        parseTrue()
      case 'f' =>
        back('f')
        parseFalse()
      case 'n' =>
        back('n')
        parseNull()
      case '{' =>
        parseObject()
      case '[' =>
        parseArray()
      case n if (n == '-' || (n >= '0' && n <= '9')) =>
        back(n)
        parseNumber()
      case e => throw JSONSyntaxException(s"excepted [string , number , null , true , false , jsonObject , jsonArray] but '$e' at row $row,column $column")
    }
  }

  private def parseItem(): (String, Any) = {
    next() match {
      case '"' =>
        back('"')
        val key = parseString()
        next() match {
          case ':' => (key, parseValue())
          case e => throw JSONSyntaxException(s"excepted ':' but '$e' at row $row,column $column")
        }
      case e => throw JSONSyntaxException(s"excepted string but '$e' at row $row,column $column")
    }
  }

  private def parseNull(): Any = {
    nextChar() match {
      case 'n' => nextChar() match {
        case 'u' => nextChar() match {
          case 'l' => nextChar() match {
            case 'l' => null
            case e => throw JSONLexerException(s"excepted null but '$e' at row $row,column $column")
          }
          case e => throw JSONLexerException(s"excepted null but $e at row $row,column $column")
        }
        case e => throw JSONLexerException(s"excepted null but $e at row $row,column $column")
      }
    }
  }

  private def parseFalse(): Boolean = {
    nextChar() match {
      case 'f' => nextChar() match {
        case 'a' => nextChar() match {
          case 'l' => nextChar() match {
            case 's' => nextChar() match {
              case 'e' => false
              case e => throw JSONLexerException(s"excepted false but '$e' at row $row,column $column")
            }
            case e => throw JSONLexerException(s"excepted false but '$e' at row $row,column $column")
          }
          case e => throw JSONLexerException(s"excepted false but '$e' at row $row,column $column")
        }
        case e => throw JSONLexerException(s"excepted false but '$e' at row $row,column $column")
      }
    }
  }

  private def parseTrue(): Boolean = {
    nextChar() match {
      case 't' => nextChar() match {
        case 'r' => nextChar() match {
          case 'u' => nextChar() match {
            case 'e' => true
            case e => throw JSONLexerException(s"excepted true but '$e' at row $row,column $column")
          }
          case e => throw JSONLexerException(s"excepted true but '$e' at row $row,column $column")
        }
        case e => throw JSONLexerException(s"excepted true but '$e' at row $row,column $column")
      }
    }
  }

  private def parseString(): String = {
    val sb: StringBuilder = new StringBuilder
    nextChar()
    var ch = nextChar()
    while (ch != '"') {
      ch match {
        case '\\' =>
          ch = nextChar()
          ch match {
            case '"' =>
              sb.append('\"')
              ch = nextChar()
            case '\\' =>
              sb.append('\\')
              ch = nextChar()
            case '/' =>
              sb.append('/')
              ch = nextChar()
            case 'b' =>
              sb.append('\b')
              ch = nextChar()
            case 'f' =>
              sb.append('\f')
              ch = nextChar()
            case 'F' =>
              sb.append('\f')
              ch = nextChar()
            case 'n' =>
              sb.append('\n')
              ch = nextChar()
            case 'r' =>
              sb.append('\r')
              ch = nextChar()
            case 't' =>
              sb.append('\t')
              ch = nextChar()
            case 'u' =>
              val u1 = nextChar()
              val u2 = nextChar()
              val u3 = nextChar()
              val u4 = nextChar()
              val s = Integer.valueOf(new String(Array(u1, u2, u3, u4)), 16).toChar
              sb.append(s)
              ch = nextChar()
            case e =>
              sb.append('\\')
              sb.append(e)
              ch = nextChar()
          }
        case e =>
          sb.append(ch)
          ch = nextChar()
      }
    }
    sb.toString()
  }

  private def parseNumber(): AnyVal = {
    val sb = new StringBuilder
    var next = nextChar()
    if (next == '-') {
      sb.append('-')
      next = nextChar()
    }
    next match {
      case '0' =>
        sb.append('0')
        next = nextChar()
      case ch if ch > '0' && ch <= '9' =>
        sb.append(next)
        next = nextChar()
        while (parseDigit(next, sb)) {
          next = nextChar()
        }
    }

    if (next == '.' || next == 'e' || next == 'E') {
      if (next == '.') {
        sb.append(next)
        next = nextChar()
        while (parseDigit(next, sb)) {
          next = nextChar()
        }
      }
      if (next == 'e' || next == 'E') {
        sb.append(next)
        next = nextChar()
        if (next == '+' || next == '-') {
          sb.append(next)
          next = nextChar()
        }
        while (parseDigit(next, sb)) {
          next = nextChar()
        }
      }
      back(next)
      sb.toString.toDouble
    } else {
      back(next)
      sb.toString.toLong match {
        case value if value >= Int.MinValue && value <= Int.MaxValue => sb.toString.toInt
        case value => value
      }
    }
  }

  private def parseDigit(c: Char, sb: StringBuilder): Boolean = {
    c match {
      case c if c >= '0' && c <= '9' =>
        sb.append(c)
        true
      case e =>
        false
    }
  }

  private def nextChar(): Char = {
    if (backBuffer.nonEmpty) {
      column = backCharPosition.head._1
      row = backCharPosition.head._2
      backCharPosition = backCharPosition.tail
      val c = backBuffer.head
      backBuffer = backBuffer.tail
      c
    } else {
      column += 1
      if (it.hasNext) {
        it.next()
      } else {
        throw JSONSyntaxException(s"excepted a char but stream ended at row $row,column $column")
      }
    }
  }

  private def next(): Char = {
    if (backBuffer.nonEmpty) {
      column = backCharPosition.head._1
      row = backCharPosition.head._2
      backCharPosition = backCharPosition.tail
      val c = backBuffer.head
      backBuffer = backBuffer.tail
      c
    } else {
      if (it.hasNext) {
        var c = it.next()
        while (ignoreLetter(c)) {
          if (it.hasNext) {
            c = it.next()
          } else {
            throw JSONSyntaxException(s"excepted a char but stream ended at row $row,column $column")
          }
        }
        column += 1
        c
      } else {
        throw JSONSyntaxException(s"excepted a char but stream ended  at row $row,column $column")
      }
    }
  }

  private def ignoreLetter(c: Char): Boolean = {
    c match {
      case ' ' =>
        column += 1
        return true
      case '\r' =>
        column += 1
        return true
      case '\n' =>
        row += 1
        column = 0
        return true
      case '\t' =>
        column += 4
        return true
      case _ => return false
    }
  }

  private def back(char: Char): Unit = {
    var c = char
    while (ignoreLetter(c)) {
      c = it.next()
    }
    column += 1
    backBuffer = backBuffer :+ c
    backCharPosition = backCharPosition :+(column, row)
  }
}

object RuleType extends Enumeration {
  type RuleType = Value
  val NORMAL_TOKEN, PATH_TOKEN, CURRENT_PATH_TOKEN = Value
}

class JSONPointerParser(str: String) {

  private val it: Iterator[Char] = str.iterator
  private var backBuffer = List.empty[Char]
  private var isPathToken = false

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
        case '.' =>
          if (hasNext()) {
            var ch = next()
            ch match {
              case '.' =>
                if (hasNext()) {
                  ch = next()
                  if (ch == '/') {
                    back('/')
                    isPathToken = true
                    Rule("..", RuleType.PATH_TOKEN) +: parsePath()
                  } else {
                    throw JSONPointerSyntaxException(s"excepted '/' but '$ch'")
                  }
                } else {
                  throw JSONPointerSyntaxException(s"EOF but parser wasn't finished")
                }
              case '/' =>
                back('/')
                Rule(".", RuleType.CURRENT_PATH_TOKEN) +: parsePath()
              case e => throw JSONPointerSyntaxException(s"excepted ['/' or '.'] but '$e'")
            }
          } else {
            throw JSONPointerSyntaxException(s"EOF but parser wasn't finished")
          }
        case e => throw JSONPointerSyntaxException(s"excepted ['/' or '.'] but '$e'")
      }
    }
  }

  private def parseRule(): Rule = {
    if (!hasNext()) {
      isPathToken = false
      return Rule("", RuleType.NORMAL_TOKEN)
    }
    var ch = next()
    if (ch == '/') {
      isPathToken = false
      back('/')
      Rule("", RuleType.NORMAL_TOKEN)
    } else if (ch == '.') {
      val sb = new StringBuilder
      sb.append(ch)
      if (!hasNext()) {
        isPathToken = false
        Rule(sb.toString, RuleType.NORMAL_TOKEN)
      } else {
        ch = next()
        if (ch == '.') {
          sb.append(ch)
          if (!hasNext()) {
            isPathToken = false
            Rule(sb.toString, RuleType.NORMAL_TOKEN)
          } else {
            ch = next()
            if (ch == '/') {
              back('/')
              if (!isPathToken) {
                Rule(sb.toString, RuleType.NORMAL_TOKEN)
              } else {
                isPathToken = true
                Rule(sb.toString, RuleType.PATH_TOKEN)
              }
            } else {
              parseString(ch, sb)
              isPathToken = false
              Rule(sb.toString, RuleType.NORMAL_TOKEN)
            }
          }
        } else {
          parseString(ch, sb)
          isPathToken = false
          Rule(sb.toString, RuleType.NORMAL_TOKEN)
        }
      }
    } else {
      val sb = new StringBuilder
      parseString(ch, sb)
      isPathToken = false
      Rule(sb.toString, RuleType.NORMAL_TOKEN)
    }
  }

  private def parseString(ch: Char, sb: StringBuilder): Unit = {
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
          } else if (c == '2') {
            sb.append("..")
          } else {
            sb.append('~')
            sb.append(c)
          }
        } else {
          sb.append('~')
        }
      } else {
        sb.append(c)
      }
    }
    if (!hasNext()) {
    }
    if (c == '/') {
      back(c)
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

class JSONPointer(str: String) {
  private val json: JSONType = JSONParser(str).parser()
  private var original = List.empty[Rule]

  def path(path: String): Any = {
    var temp: Any = json
    var rules = JSONPointerParser(path).parsePath()
    rules match {
      case Rule(rule: Any, ruleType: RuleType) :: tail =>
        ruleType match {
          case RuleType.CURRENT_PATH_TOKEN => rules = original ::: tail
          case RuleType.PATH_TOKEN => rules = detectPath(original, rules)
          case RuleType.NORMAL_TOKEN => original = rules
        }
      case Nil =>
    }
    rules.foreach(rule => temp = filter(rule, temp))
    temp
  }

  private def detectPath(original: List[Rule], current: List[Rule]): List[Rule] = {
    current match {
      case Rule(rule: Any, ruleType: RuleType) :: tail => ruleType match {
        case RuleType.PATH_TOKEN => detectPath(original.reverse.tail.reverse, tail)
        case e => original ::: current
      }
      case Nil => original
    }
  }

  def reduce(obj: Any): Any = {
    obj match {
      case list: List[Any] =>
        val rs = list.filter(_ != NotFound).map(reduce(_))
        rs.size match {
          case 1 => rs(0)
          case _ => rs
        }
      case obj => obj
    }
  }

  private def filter(rule: Rule, temp: Any): Any = {
    temp match {
      case jsonAry: JSONArray =>
        rule.rule match {
          case str: String =>
            val arySize = jsonAry.list.size
            if (str.indexOf(':') >= 0) {
              val from: Int = parseArrayIndex(str.substring(0, str.indexOf(":")), arySize, 0)
              val until = parseArrayIndex(str.substring(str.indexOf(":") + 1), arySize, arySize - 1)
              if (from > until) jsonAry.list.slice(until, from + 1).reverse else jsonAry.list.slice(from, until + 1)
            } else if (str.indexOf(',') >= 0) {
              val ary = str.split(",")
              if (ary.length < 2) {
                val size = ary.length
                throw JSONPointerException(s"excepted >=2 numbers but size is $size", null)
              }

              ary.map(e => {
                jsonAry.list(parseArrayIndex(e, arySize, throw JSONPointerException(s"excepted a number but '$e'", null)))
              }).toList

            } else if (str.trim == "*") {
              jsonAry.list
            } else {
              jsonAry.list(parseArrayIndex(str, arySize, throw JSONPointerException(s"excepted a number but '$str'", null)))
            }
        }
      case jsonObj: JSONObject =>
        rule.rule match {
          case str: String => jsonObj.map.getOrElse(str, {
            if (str.indexOf(",") >= 0) {
              val keys = str.split(",").toList
              keys.map(jsonObj.map.getOrElse(_, NotFound))
            } else if (str.trim == "*") {
              jsonObj.map.values.toList
            } else {
              NotFound
            }
          })
        }
      case list: List[Any] => list.map(filter(rule, _))
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
        throw JSONPointerException(s"out of range,array size is $size", null)
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
          throw JSONPointerException(s"excepted a number but '$str'", null)
        }
      }
      next match {
        case '0' =>
          sb.append('0')
          if (!hasNext) {
            0
          } else {
            throw JSONPointerException(s"excepted a number but '$str'", null)
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
              throw JSONPointerException(s"excepted a number but '$str'", null)
            }
          }
          str2int(sb.toString(), size)

        case _ => throw JSONPointerException(s"excepted a number but '$str'", null)
      }
    }

  }
}
