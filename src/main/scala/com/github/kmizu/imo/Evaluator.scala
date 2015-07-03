package com.github.kmizu.imo

import java.util.Scanner

class Evaluator() {
  private[this] lazy val scanner = new Scanner(System.in)

  private[this] lazy val builtinEnvironment: Environment = {
    Environment(
      Symbol("+"),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] + rhs.asInstanceOf[Int]
        }
      },
      None
    ).updated(
      Symbol("-"),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] - rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      Symbol("*"),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] * rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      Symbol("/"),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] / rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      Symbol("%"),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] % rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      Symbol("<"),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] < rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      Symbol(">"),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] > rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      Symbol("<="),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] <= rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      Symbol(">="),
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] >= rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      Symbol("parse_int"),
      new NativeFunction {
        override def apply(arg: Any): Any = arg.asInstanceOf[String].toInt
      }
    ).updated(
      Symbol("read_line"),
      new Action {
        def perform(): Any = scanner.nextLine()
      }
    ).updated(
      Symbol("println"),
      new NativeFunction {
        override def apply(arg: Any): Any = new Action {
          override def perform(): Any = {
            println(arg)
            ()
          }
        }
      }
    ).updated(
      Symbol("print"),
      new NativeFunction {
        override def apply(arg: Any): Any = new Action {
          override def perform(): Any = {
            print(arg)
            ()
          }
        }
      }
    ).updated(
      Symbol("print_number"),
      new NativeFunction {
        override def apply(arg: Any): Any = new Action {
          override def perform(): Any = {
            println(arg)
            ()
          }
        }
      }
    )
  }

  def eval(node: Prog, commandLine: String): Any = {
    val env = node.funs.foldLeft(builtinEnvironment){(e, f) =>
      e.updated(f.id, UserFunction(f.args.map{_.id}, f.exp, null))
    }
    var e = Option(env)
    while(e != None) {
      val value = e.get.value
      if(value.isInstanceOf[UserFunction]) {
        value.asInstanceOf[UserFunction].env = env
      }
      e = e.get.next
    }
    val main = env.lookup(Symbol("main")).get.asInstanceOf[UserFunction]
    val toplevel = env.updated(main.args(0), commandLine)
    eval(main.exp, toplevel)
  }

  def eval(exp: Exp, env: Environment): Any = exp match {
    case Ref(_, id) =>
      env.lookup(id) match {
        case Some(value) => value
        case None => sys.error(s"variable ${id} not found")
      }
    case If(_, cond, lhs, rhs) =>
      if(eval(cond, env).asInstanceOf[Boolean]) eval(lhs, env) else eval(rhs, env)
    case Equal(_, lhs, rhs) =>
      eval(lhs, env) == eval(rhs, env)
    case NotEqual(_, lhs, rhs) =>
      eval(lhs, env) != eval(rhs, env)
    case And(_, lhs, rhs) =>
      eval(lhs, env).asInstanceOf[Boolean] && eval(rhs, env).asInstanceOf[Boolean]
    case Or(_, lhs, rhs) =>
      eval(lhs, env).asInstanceOf[Boolean] || eval(rhs, env).asInstanceOf[Boolean]
    case BoolNode(_, value) =>
      value
    case StrNode(_, value) =>
      value
    case IntNode(_, value) =>
      value
    case UnitNode(_) =>
      ()
  }
}
