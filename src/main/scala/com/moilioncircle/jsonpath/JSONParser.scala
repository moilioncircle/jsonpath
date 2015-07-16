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
abstract class JSONType

case class JSONObject(obj: Map[String, _]) extends JSONType

case class JSONArray(list: List[_]) extends JSONType

object JSONParser {
  def apply(json: String): JSONParser = new JSONParser(json)

  def apply(jsonIterator: Iterator[Char]): JSONParser = new JSONParser(jsonIterator)
}

class JSONParser {
  def this(json: String) {
    this()
    it = json.iterator
  }

  def this(json: Iterator[Char]) {
    this()
    it = json
  }

  private var it: Iterator[Char] = _
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
