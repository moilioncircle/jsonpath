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
 * the definition of exceptions which used in json pointer.
 *
 * @author leon chen
 * @version 0.0.1
 * @since 1.0.0
 */
@SerialVersionUID(1L)
class JSONSyntaxException protected(message: String) extends RuntimeException(message: String) {

}

object JSONSyntaxException {
  def apply(message: String): JSONSyntaxException = new JSONSyntaxException(message)
}

@SerialVersionUID(1L)
class JSONPointerException protected(message: String) extends RuntimeException(message: String) {

}

object JSONPointerException {
  def apply(message: String): JSONPointerException = new JSONPointerException(message)
}

@SerialVersionUID(1L)
class JSONPointerSyntaxException protected(message: String) extends RuntimeException(message: String) {

}

object JSONPointerSyntaxException {
  def apply(message: String): JSONPointerSyntaxException = new JSONPointerSyntaxException(message)
}

@SerialVersionUID(1L)
class JSONLexerException protected(message: String) extends RuntimeException(message: String) {

}

object JSONLexerException {
  def apply(message: String): JSONLexerException = new JSONLexerException(message)
}
