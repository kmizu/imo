package com.github.kmizu.imo

import scala.util.parsing.combinator.Parsers


import scala.util.parsing.combinator._
import scala.util.parsing.input.{CharSequenceReader, StreamReader}
import scala.util.parsing.input.Position
import java.io._

/**
 * This object provides a parser that parses strings in Pegex and translates
 * them into ASTs of PEGEX (which is like PEGs).
 * @author Kota Mizushima
 *
 */
class IMoParser() {

  /**
   * This exception is thrown in the case of a parsing failure
   * @param pos the position where the parsing failed
   * @param msg error message
   */
  case class ParseException(pos: Pos, msg: String) extends Exception(pos.row + ", " + pos.col + ":" + msg)

  private object IMoParsers extends RegexParsers {
    override def skipWhitespace = false

    private def chr(c: Char): Parser[Char] = c

    private def token(s: String): Parser[String] = s <~ SPACING

    private def not[T](p: => Parser[T], msg: String): Parser[Unit] = {
      not(p) | failure(msg)
    }

    private val any: Parser[Char] = elem(".", c => c != CharSequenceReader.EofCh)
    lazy val END_OF_LINE = chr('\r') ~ chr('\n') | chr('\n') | chr('\r')
    lazy val COMMENT = ("//" | "#!") ~> (not(END_OF_LINE) ~> any).* <~ END_OF_LINE
    lazy val SPACING = """\s""".r | COMMENT

    lazy val PLUS = token("+")
    lazy val MINUS = token("-")
    lazy val ASTER = token("*")
    lazy val SLASH = token("/")
    lazy val PERCENT = token("%")
    lazy val DOLLAR = token("$")
    lazy val LT = token("<")
    lazy val GT = token(">")
    lazy val LEQ = token("<=")
    lazy val GEQ = token(">=")
    lazy val RARROW = token("->")
    lazy val LARROW = token("<-")
    lazy val BIND = token(">>=")
    lazy val CONCAT = token(">>")
    lazy val ASSIGN = token("=")
    lazy val EQ = token("==")
    lazy val NOTEQ = token("/=")
    lazy val PLUSPLUS = token("++")
    lazy val MINUSMINUS = token("--")
    lazy val COLON = token(":")
    lazy val SEMI = token(";")
    lazy val COLON2 = token("::")
    lazy val SHARP = token("#")
    lazy val DOT = token(".")
    lazy val LBRACE = token("{")
    lazy val RBRACE = token("}")
    lazy val LPAREN = token("(")
    lazy val RPAREN = token(")")
    lazy val COMMA = token(",")
    lazy val LBRACKET = token("[")
    lazy val RBRACKET = token("]")
    lazy val QUESTION = token("?")
    lazy val LAMBDA = token("\\")

    lazy val K_AND = token("and")
    lazy val K_BOOL = token("bool")
    lazy val K_DEF = token("def")
    lazy val K_DO = token("do")
    lazy val K_ELSE = token("else")
    lazy val K_IF = token("if")
    lazy val K_LET = token("let")
    lazy val K_IN = token("in")
    lazy val K_IO = token("io")
    lazy val K_INT = token("int")
    lazy val K_NOT = token("not")
    lazy val K_OR = token("or")
    lazy val K_RETURN = token("return")
    lazy val K_STRING = token("string")
    lazy val K_TRUE = token("true")
    lazy val K_FALSE = token("false")

    lazy val INTEGER: Parser[String] = DECIMAL_LITERAL <~ "L".? | HEX_LITERAL <~ "L".? | OCTAL_LITERAL <~ "L".?
    lazy val DECIMAL_LITERAL: Parser[String] = """[1-9][0-9]*|0""".r
    lazy val HEX_LITERAL: Parser[String] = """0(x|X)[0-9a-fA-F]+""".r
    lazy val OCTAL_LITERAL: Parser[String] = """0([0-7]*""".r
    lazy val CHARACTER: Parser[String] = (
      "'" ~> (not("'" | "\\" | "\n" | "\r") ~> any) <~ "'" ^^ (_.toString) |
        "'" ~> """[ntbrf\'"]|[0-7]([0-7])?|[0-3][0-7][0-7]""".r <~ "'"
      )
    lazy val STRING: Parser[String] = (
      "\"" ~> ((not("'" | "\\" | "\n" | "\n") ~> any ^^ (_.toString) | """[ntbrf\'"]|[0-7]([0-7])?|[0-3][0-7][0-7]""".r).* ^^ (_.mkString)) <~ "'"
      )
    lazy val ID: Parser[String] = """[a-zA-Z_][a-zA-Z_0-9]*""".r

    lazy val program: Parser[Prog] = ???

    lazy val function: Parser[Fun] = ???

    lazy val tpe: Parser[Type] = ???

    lazy val basic_type: Parser[Type] = ???

    lazy val formal_parameter: Parser[Arg] = ???

    lazy val expression: Parser[Exp] = ???

    lazy val anonymous_function: Parser[Exp] = ???

    lazy val bindable: Parser[Exp] = ???

    lazy val application: Parser[Exp] = ???

    lazy val logical_or: Parser[Exp] = ???

    lazy val logical_and: Parser[Exp] = ???

    lazy val equal: Parser[Exp] = ???

    lazy val comparative: Parser[Exp] = ???

    lazy val additive: Parser[Exp] = ???

    lazy val unary_prefix: Parser[Exp] = ???

    lazy val primary_suffix: Parser[Exp] = ???

    lazy val primary: Parser[Exp] = ???

    lazy val int_literal: Parser[Exp] = ???

    lazy val bool_literal: Parser[Exp] = ???

    lazy val bind: Parser[Def] = ???

    lazy val let_expression: Parser[Exp] = ???

    lazy val if_expression: Parser[Exp] = ???
  }

  /**
   * Parses an input from `content` and returns the `Prog` instance, which is the parse result.
   * @param fileName
   * @param content
   * @return `Grammar` instance
   */
  def parse(fileName: String, content: java.io.Reader): Prog = {
    IMoParsers.program(StreamReader(content)) match {
      case IMoParsers.Success(node, _) => node
      case IMoParsers.Failure(msg, rest) =>
        val pos = rest.pos
        throw new ParseException(Pos(pos.line, pos.column), msg)
      case IMoParsers.Error(msg, rest) =>
        val pos = rest.pos
        throw new ParseException(Pos(pos.line, pos.column), msg)
    }
  }

  /**
   * Parses a `input` and returns the `Prog` instance, which is the parse result.
   * @param input input string
   * @return `Grammar` instance
   */
  def parse(input: String): Prog = {
    parse("", new StringReader(input))
  }

  def main(args: Array[String]) {
    val g = parse(args(0), new FileReader(args(0)))
    println(g)
  }


}
