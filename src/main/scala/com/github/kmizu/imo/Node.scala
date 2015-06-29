package com.github.kmizu.imo

abstract class Node {
  val pos: Pos
}
abstract class Exp extends Node

case class Prog(pos: Pos, funs: List[Fun]) extends Node
case class Def(pos: Pos, id: Symbol, exp: Exp) extends Node
case class Arg(pos: Pos, id: Symbol, tpe: Type) extends Node
case class Fun(pos: Pos, id: Symbol, args: List[Arg], ret: Type, exp: Exp) extends Node
case class AnonFun(pos: Pos, arg: Arg, exp: Exp) extends Exp
case class Ref(pos: Pos, id: Symbol) extends Exp
case class App(pos: Pos, fn: Exp, arg: Exp) extends Exp
case class Let(pos: Pos, defs: List[Def], exp: Exp) extends Exp
case class If(pos: Pos, cond: Exp, lhs: Exp, rhs: Exp) extends Exp
case class And(pos: Pos, lhs: Exp, rhs: Exp) extends Exp
case class Bind(pos: Pos, lhs: Exp, rhs: Exp) extends Exp
case class Concat(pos: Pos, lhs: Exp, rhs: Exp) extends Exp
case class Return(pos: Pos, exp: Exp) extends Exp
case class Or(pos: Pos, lhs: Exp, rhs: Exp) extends Exp
case class Equal(pos: Pos, lhs: Exp, rhs: Exp) extends Exp
case class NotEqual(pos: Pos, lhs: Exp, rhs: Exp) extends Exp
case class Int(pos: Pos, value: Int) extends Exp
case class Str(pos: Pos, value: String) extends Exp
case class Bool(pos: Pos, value: Boolean) extends Exp
case class Unit(pos: Pos) extends Exp
