package com.github.kmizu.imo

import java.util.Scanner

class Evaluator() {
  private[this] val scanner = new Scanner(System.in)

  private[this] def prepareBuiltinEnvironment: Environment = {
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
}
