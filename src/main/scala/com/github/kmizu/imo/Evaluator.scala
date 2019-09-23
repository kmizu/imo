package com.github.kmizu.imo

import java.util.Scanner

object Evaluator {
  private[this] lazy val scanner = new Scanner(System.in)

  private[this] lazy val builtinEnvironment: Environment = {
    Environment(
      "+",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] + rhs.asInstanceOf[Int]
        }
      },
      None
    ).updated(
      "-",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] - rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      "*",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] * rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      "/",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] / rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      "%",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] % rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      "<",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] < rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      ">",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] > rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      "<=",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] <= rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      ">=",
      new NativeFunction {
        def apply(lhs: Any): Any = new NativeFunction {
          def apply(rhs: Any): Any = lhs.asInstanceOf[Int] >= rhs.asInstanceOf[Int]
        }
      }
    ).updated(
      "parse_int",
      new NativeFunction {
        override def apply(arg: Any): Any = arg.asInstanceOf[String].toInt
      }
    ).updated(
      "read_line",
      new Action {
        def perform(): Any = scanner.nextLine()
      }
    ).updated(
      "println",
      new NativeFunction {
        override def apply(arg: Any): Any = new Action {
          override def perform(): Any = {
            println(arg)
            ()
          }
        }
      }
    ).updated(
      "print",
      new NativeFunction {
        override def apply(arg: Any): Any = new Action {
          override def perform(): Any = {
            print(arg)
            ()
          }
        }
      }
    ).updated(
      "print_number",
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
    val env = node.funs.foldLeft(builtinEnvironment){(e: Environment, f) =>
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
    val main = env.lookup("main").get.asInstanceOf[UserFunction]
    val toplevel = env.updated(main.args(0), commandLine)
    eval(main.exp, toplevel) match {
      case action:Action => action.perform()
      case otherwise => otherwise
    }
  }

  def eval(exp: Exp, env: Environment): Any = exp match {
    case Bind(_, lhs, rhs) =>
      val lval = eval(lhs, env)
      val rval = eval(rhs, env)
      new Action {
        override def perform(): Any = {
          val arg = lval.asInstanceOf[Action].perform()
          apply(rval.asInstanceOf[UserFunction], arg).asInstanceOf[Action].perform()
        }
      }
    case App(_, tfun, targ) =>
      val fun = eval(tfun, env)
      val arg = eval(targ, env)
      fun match {
        case fun:NativeFunction => fun.apply(arg)
        case _ => apply(fun.asInstanceOf[UserFunction], arg)
      }
    case Return(_, exp) =>
      val lval = eval(exp, env)
      new Action {
        override def perform(): Any = lval
      }
    case AnonFun(_, arg, exp) =>
      UserFunction(List(arg.id), exp, env)
    case Concat(_, lhs, rhs) =>
      val lval = eval(lhs, env)
      val rval = eval(rhs, env)
      new Action {
        override def perform(): Any = {
          val arg = lval.asInstanceOf[Action].perform()
          rval.asInstanceOf[Action].perform()
        }
      }
    case Let(_, defs, exp) =>
      var local = env
      for(aDef <- defs) {
        local = local.updated(aDef.id, eval(aDef.exp, local))
      }
      eval(exp, local)
    case Ref(_, id) =>
      env.lookup(id) match {
        case Some(value) =>
          value
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

  private def apply(uf: UserFunction, arg: Any): Any = {
    val local = Environment(uf.args(0), arg, Some(uf.env))
    if (uf.args.size == 1) {
      eval(uf.exp, local)
    } else {
      UserFunction(uf.args.slice(1, uf.args.size), uf.exp, local)
    }
  }
}
