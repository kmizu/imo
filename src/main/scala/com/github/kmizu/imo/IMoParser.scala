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

    def app(fun: Exp, arg: Exp): App = App(fun.pos, fun, arg)
    def app2(fun: Exp, arg1: Exp, arg2: Exp): App = app(app(fun, arg1), arg2)

    lazy val loc: Parser[Pos] = Parser{reader => Success(Pos(reader.pos.line, reader.pos.column), reader)}
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
    lazy val BACK_SLASH = token("\\")
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
    lazy val K_UNIT = token("unit")
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

    lazy val program: Parser[Prog] = function.+ ^^ {funs => Prog(Pos(1, 1), funs)}

    lazy val function: Parser[Fun] = loc ~ (K_DEF ~> ID) ~ (LPAREN ~> formal_parameter.* <~ RPAREN) ~ (COLON ~> tpe <~  ASSIGN) ~ expression ^^ {
      case pos ~ id ~ args ~ ret ~ body => Fun(pos, Symbol(id), args, ret, body)
    }

    lazy val tpe: Parser[Type] = basic_type ~ (RARROW ~> tpe).? ^^ {
      case ltype ~ None => ltype
      case ltype ~ Some(rtype) => FUNCTION_TYPE(ltype, rtype)
    }

    lazy val basic_type: Parser[Type] = {
      K_BOOL ^^ {_ => BOOL_TYPE} |
      K_INT ^^ {_ => INT_TYPE} |
      K_STRING ^^ {_ => STRING_TYPE} |
      K_UNIT ^^ {_ => UNIT_TYPE} |
      K_IO ~> (LPAREN ~> tpe <~ RPAREN) ^^ {tp => IO_TYPE(tp)}
      LPAREN ~> tpe <~ RPAREN
    }

    lazy val formal_parameter: Parser[Arg] = loc ~ (ID <~ COLON) ~ tpe ^^ { case pos ~ name ~ tp => Arg(pos, Symbol(name), tp) }

    lazy val expression: Parser[Exp] = (
      let_expression |
      if_expression |
      bindable |
      anonymous_function |
      (loc <~ K_RETURN) ~ expression ^^ { case pos ~ exp => Return(pos, exp) }
    )

    lazy val anonymous_function: Parser[Exp] = (loc <~ BACK_SLASH) ~ (formal_parameter <~ DOT) ~ expression ^^ {
      case pos ~ arg ~ exp => AnonFun(pos, arg, exp)
    }

    lazy val bindable: Parser[Exp] = chainl1(application,
      BIND ^^ {_ => (l: Exp, r: Exp) => Bind(l.pos, l, r)} |
      CONCAT ^^ {_ => (l: Exp, r: Exp) => Concat(l.pos, l, r)}
    )

    lazy val application: Parser[Exp] = logical_or ~ (DOLLAR ~> application) ^^ { case l ~ r => App(l.pos, l, r) }

    lazy val logical_or: Parser[Exp] = chainl1(logical_and,
      K_OR ^^ {_ => (l: Exp, r: Exp) => Or(l.pos, l, r)}
    )

    lazy val logical_and: Parser[Exp] = chainl1(equal,
      K_AND ^^ {_ => (l: Exp, r: Exp) => And(l.pos, l, r)}
    )

    lazy val equal: Parser[Exp] = chainl1(comparative,
      EQ ^^ {_ => (l: Exp, r: Exp) => Equal(l.pos, l, r)} |
      NOTEQ ^^ {_ => (l: Exp, r: Exp) => NotEqual(l.pos, l, r)}
    )

    lazy val comparative: Parser[Exp] = chainl1(additive,
      LEQ ^^ {_ => (l: Exp, r: Exp) => app2(Ref(l.pos, Symbol("<=")), l, r) } |
      GEQ ^^ {_ => (l: Exp, r: Exp) => app2(Ref(l.pos, Symbol(">=")), l, r) } |
      LT ^^ {_ => (l: Exp, r: Exp) =>  app2(Ref(l.pos, Symbol("<")), l, r) } |
      GT ^^ {_ => (l: Exp, r: Exp) =>  app2(Ref(l.pos, Symbol(">")), l, r) }
    )

    lazy val additive: Parser[Exp] = chainl1(unary_prefix,
      PLUS ^^ {_ => (l: Exp, r: Exp) => app2(Ref(l.pos, Symbol("+")), l, r) } |
      MINUS ^^ {_ => (l: Exp, r: Exp) => app2(Ref(l.pos, Symbol("-")), l, r) }
    )

    lazy val unary_prefix: Parser[Exp] = (
      PLUS ~> unary_prefix ^^ {e => app(Ref(e.pos, Symbol("u+")), e) } |
      MINUS ~> unary_prefix ^^ {e => app(Ref(e.pos, Symbol("u-")), e)} |
      K_NOT ~> unary_prefix ^^ {e => app(Ref(e.pos, Symbol("not")), e)} |
      multitive
    )

    lazy val multitive: Parser[Exp] = chainl1(primary_suffix,
      ASTER ^^ {_ => (l: Exp, r: Exp) => app2(Ref(l.pos, Symbol("*")), l, r) } |
      SLASH ^^ {_ => (l: Exp, r: Exp) => app2(Ref(l.pos, Symbol("/")), l, r) } |
      PERCENT ^^ {_ => (l: Exp, r: Exp) => app2(Ref(l.pos, Symbol("%")), l, r) }
    )

    lazy val primary_suffix: Parser[Exp] = primary ~ primary.* ^^ { case a ~ as => as.foldLeft(a){(r, e) => app(r, e)}}

    lazy val primary: Parser[Exp] = (
      loc ~ ID ^^ { case pos ~ id => Ref(pos, Symbol(id))} |
      int_literal |
      string_literal |
      bool_literal |
      (LPAREN ~> expression <~ RPAREN)
    )

    lazy val int_literal: Parser[Exp] = loc ~ INTEGER ^^ { case pos ~ value => IntNode(pos, value.toInt) }

    lazy val bool_literal: Parser[Exp] = loc <~ K_TRUE ^^ { case pos => BoolNode(pos, true)} | loc <~ K_FALSE ^^ { case pos => BoolNode(pos, false) }

    lazy val string_literal: Parser[Exp] = loc ~ STRING ^^ { case pos ~ value => StrNode(pos, value) }

    lazy val bind: Parser[Def] = loc ~ (ID <~ ASSIGN) ~ expression ^^ { case pos ~ id ~ exp => Def(pos, Symbol(id), exp) }

    lazy val let_expression: Parser[Exp] = (loc <~ K_LET) ~ bind ~ (COMMA ~> bind).* ~ (K_IN ~> expression) ^^ {
      case pos ~ b ~ bs ~ exp => Let(pos, b::bs, exp)
    }

    lazy val if_expression: Parser[Exp] = (loc <~ K_IF) ~ (LPAREN ~> expression <~ RPAREN) ~ expression ~ (K_ELSE ~> expression) ^^ {
      case pos ~ cond ~ thenExp ~ elseExp => If(pos, cond, thenExp, elseExp)
    }
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
