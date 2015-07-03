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
      if(e.get.isInstanceOf[UserFunction]) {
        e.get.asInstanceOf[UserFunction].env = env
      }
      e = e.get.next
    }
    val main = env.lookup(Symbol("main")).get.asInstanceOf[UserFunction]
    val toplevel = env.updated(main.args(0), commandLine)
    eval(main.exp, toplevel)
  }

  def eval(exp: Exp, env: Environment): Any = ???
}
