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
 * Created by leon on 15-6-21.
 */

import spray.json._

import scala.annotation.switch

object JSONParser {
  def apply(json: String): JSONParser = new JSONParser(json.iterator)

  def apply(json: Iterator[Char]): JSONParser = new JSONParser(json)
}

class JSONParser(it: Iterator[Char]) {

  private var column: Int = 0
  private var row: Int = 0
  private var backChar: Option[(Char, Int, Int)] = None
  private val sb: StringBuilder = new StringBuilder

  def parse(): JsValue = {
    try {
      next() match {
        case '{' => JsObject(parseObject())
        case '[' => JsArray(parseArray())
        case e => throw JSONSyntaxException(s"excepted ['[' , '{'] but '$e' at row $row,column $column")
      }
    } catch {
      case e: NoSuchElementException => throw JSONSyntaxException(s"excepted a char but stream ended at row $row,column $column")
    }
  }

  private def parseObject(): Map[String, JsValue] = {
    var map = Map.newBuilder[String, JsValue]
    next() match {
      case '}' => map.result()
      case c =>
        map += (c match {
          case '"' =>
            val key = parseString()
            next() match {
              case ':' => (key, parseValue(next()))
              case e => throw JSONSyntaxException(s"excepted ':' but '$e' at row $row,column $column")
            }
          case e => throw JSONSyntaxException(s"excepted string but '$e' at row $row,column $column")
        })
        next() match {
          case ',' =>
            var ch = ','
            while (ch == ',') {
              map += (next() match {
                case '"' =>
                  val key = parseString()
                  next() match {
                    case ':' => (key, parseValue(next()))
                    case e => throw JSONSyntaxException(s"excepted ':' but '$e' at row $row,column $column")
                  }
                case e => throw JSONSyntaxException(s"excepted string but '$e' at row $row,column $column")
              })
              ch = next()
            }
            ch match {
              case '}' =>
                map.result()
              case e => throw JSONSyntaxException(s"excepted '}' but '$e' at row $row,column $column")
            }
          case '}' =>
            map.result()
          case e => throw JSONSyntaxException(s"excepted [',' , '}'] but '$e' at row $row,column $column")
        }
    }
  }

  private def parseArray(): Vector[JsValue] = {
    var list = Vector.newBuilder[JsValue]
    next() match {
      case ']' =>
        list.result()
      case ch =>
        list += parseValue(ch)
        next() match {
          case ',' =>
            var ch = ','
            while (ch == ',') {
              list += parseValue(next())
              ch = next()
            }
            ch match {
              case ']' =>
                list.result()
              case e => throw JSONSyntaxException(s"excepted ']' but '$e' at row $row,column $column")
            }
          case ']' =>
            list.result()
          case e => throw JSONSyntaxException(s"excepted [',' , ']'] but '$e' at row $row,column $column")
        }
    }
  }

  private def parseValue(ch: Char): JsValue = {
    (ch: @switch) match {
      case '"' =>
        JsString(parseString())
      case n@('0' | '1' | '2' | '3' | '4' | '5' | '6' | '7' | '8' | '9' | '-') =>
        JsNumber(parseNumber(n))
      case 't' =>
        parseTrue(ch)
        JsTrue
      case 'f' =>
        parseFalse(ch)
        JsFalse
      case 'n' =>
        parseNull(ch)
        JsNull
      case '{' =>
        JsObject(parseObject())
      case '[' =>
        JsArray(parseArray())
      case e => throw JSONSyntaxException(s"excepted [string , number , null , true , false , jsonObject , jsonArray] but '$e' at row $row,column $column")
    }
  }

  @inline
  private def parseNull(ch: Char): Unit = {
    ch match {
      case 'n' => nextChar() match {
        case 'u' => nextChar() match {
          case 'l' => nextChar() match {
            case 'l' =>
            case e => throw JSONLexerException(s"excepted null but '$e' at row $row,column $column")
          }
          case e => throw JSONLexerException(s"excepted null but $e at row $row,column $column")
        }
        case e => throw JSONLexerException(s"excepted null but $e at row $row,column $column")
      }
    }
  }

  @inline
  private def parseFalse(ch: Char): Unit = {
    ch match {
      case 'f' => nextChar() match {
        case 'a' => nextChar() match {
          case 'l' => nextChar() match {
            case 's' => nextChar() match {
              case 'e' =>
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

  @inline
  private def parseTrue(ch: Char): Unit = {
    ch match {
      case 't' => nextChar() match {
        case 'r' => nextChar() match {
          case 'u' => nextChar() match {
            case 'e' =>
            case e => throw JSONLexerException(s"excepted true but '$e' at row $row,column $column")
          }
          case e => throw JSONLexerException(s"excepted true but '$e' at row $row,column $column")
        }
        case e => throw JSONLexerException(s"excepted true but '$e' at row $row,column $column")
      }
    }
  }

  @inline
  private def parseString(): String = {
    sb.setLength(0)
    var next = nextChar()
    while (next != '"') {
      next match {
        case '\\' =>
          next = nextChar()
          (next : @switch) match {
            case '"' =>
              sb.append('\"')
              next = nextChar()
            case '\\' =>
              sb.append('\\')
              next = nextChar()
            case '/' =>
              sb.append('/')
              next = nextChar()
            case 'b' =>
              sb.append('\b')
              next = nextChar()
            case 'f' =>
              sb.append('\f')
              next = nextChar()
            case 'F' =>
              sb.append('\f')
              next = nextChar()
            case 'n' =>
              sb.append('\n')
              next = nextChar()
            case 'r' =>
              sb.append('\r')
              next = nextChar()
            case 't' =>
              sb.append('\t')
              next = nextChar()
            case 'u' =>
              val s = Integer.valueOf(new String(Array(nextChar(), nextChar(), nextChar(), nextChar())), 16).toChar
              sb.append(s)
              next = nextChar()
            case e =>
              sb.append('\\')
              sb.append(e)
              next = nextChar()
          }
        case e =>
          sb.append(next)
          next = nextChar()
      }
    }
    sb.toString()
  }

  @inline
  private def parseNumber(ch: Char): String = {
    sb.setLength(0)
    var next = ch
    next match {
      case '-' =>
        sb.append('-')
        next = nextChar()
      case _ =>
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

    next match {
      case '.' =>
        sb.append(next)
        next = nextChar()
        while (parseDigit(next, sb)) {
          next = nextChar()
        }
      case _ =>
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
    sb.toString
  }

  @inline
  private def parseDigit(c: Char, sb: StringBuilder): Boolean = {
    c match {
      case c if c >= '0' && c <= '9' =>
        sb.append(c)
        true
      case e =>
        false
    }
  }

  @inline
  private def nextChar(): Char = {
    if (backChar.nonEmpty) {
      column = backChar.get._2
      row = backChar.get._3
      val c = backChar.get._1
      backChar = None
      c
    } else {
      column += 1
      it.next()
    }
  }

  @inline
  private def next(): Char = {
    if (backChar.nonEmpty) {
      column = backChar.get._2
      row = backChar.get._3
      val c = backChar.get._1
      backChar = None
      c
    } else {
      var c = it.next()
      while (ignoreLetter(c)) {
        c = it.next()
      }
      column += 1
      c
    }
  }

  @inline
  private def ignoreLetter(c: Char): Boolean = {
    (c: @switch) match {
      case ' ' =>
        column += 1
        true
      case '\r' =>
        column += 1
        true
      case '\n' =>
        row += 1
        column = 0
        true
      case '\t' =>
        column += 4
        true
      case _ => false
    }
  }

  @inline
  private def back(char: Char): Unit = {
    var c = char
    while (ignoreLetter(c)) {
      c = it.next()
    }
    column += 1
    backChar = Some((c, column, row))
  }
}
