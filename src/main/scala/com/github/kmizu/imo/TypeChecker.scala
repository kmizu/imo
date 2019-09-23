package com.github.kmizu.imo

object TypeChecker {
  def typeCheck(node: Prog): Boolean = {
    var env: TypeEnvironment = builtinEnv
    var errorFound = false
    for(fun <- node.funs){
      val args = fun.args
      var `type`: Type = fun.ret
      for(i <- args.size - 1 to 0 by - 1) {
        `type` = FUNCTION_TYPE(args(i).tpe, `type`)
      }
      env = env.updated(fun.id, `type`)
    }
    for(fun <- node.funs){
      val args = fun.args
      var local = env
      for(i <- args.size - 1 to 0 by - 1){
        val arg = args(i)
        local = local.updated(arg.id, arg.tpe)
      }
      val `type` = doType(fun.exp, local)
      if(`type` == ERROR_TYPE) errorFound = true;
      if(`type` != fun.ret){
        reportError(fun.pos, fun.ret, `type`)
        errorFound = true
      }
    }
    !errorFound
  }

  private def reportError(pos: Pos, expected: Type, actual: Type): Unit = {
    Console.err.println(s"${pos.row}, ${pos.col}: expected: ${expected} actual: ${actual}")
  }

  private def put(env: TypeEnvironment, id: String, `type`: Type): TypeEnvironment = {
    if(env == null) {
      new TypeEnvironment(id, `type`, null)
    } else {
      env.updated(id, `type`)
    }
  }

  private def builtinEnv: TypeEnvironment = {
    var env: TypeEnvironment = null
    env = put(env, "+", fun(INT, fun(INT, INT)));
    env = put(env, "-", fun(INT, fun(INT, INT)));
    env = put(env, "*", fun(INT, fun(INT, INT)));
    env = put(env, "/", fun(INT, fun(INT, INT)));
    env = put(env, "%", fun(INT, fun(INT, INT)));
    env = put(env, "/", fun(INT, fun(INT, INT)));
    env = put(env, "<", fun(INT, fun(INT, BOOL)));
    env = put(env, ">", fun(INT, fun(INT, BOOL)));
    env = put(env, "<=", fun(INT, fun(INT, BOOL)));
    env = put(env, ">=", fun(INT, fun(INT, BOOL)));
    env = put(env, "parse_int", fun(STRING, INT));
    env = put(env, "print", fun(STRING, io(UNIT)));
    env = put(env, "println", fun(STRING, io(UNIT)));
    env = put(env, "print_number", fun(INT, io(UNIT)));
    env = put(env, "read_line", io(STRING));
    env
  }
  def doType(exp: Exp, env: TypeEnvironment): Type = exp match {
    case BoolNode(_, _) => BOOL
    case IntNode(_, _) => INT
    case StrNode(_, _) => STRING
    case UnitNode(_) => UNIT
    case If(pos, cond, lhs, rhs) =>
      var errorFound = false
      val condType = doType(cond, env)
      if (condType != BOOL && condType != ERROR) {
        incompatible(cond.pos, BOOL, condType);
        errorFound = true
      }
      val lhsType = doType(lhs, env)
      val rhsType = doType(rhs, env)
      if (lhsType != ERROR && rhsType != ERROR && lhsType != rhsType) {
        incompatible(pos, lhsType, rhsType);
        errorFound = true
      }
      if (errorFound) ERROR else lhsType
    case Ref(pos, id) =>
      var localEnv = env
      while (localEnv != null) {
        if (id == localEnv.id) return localEnv.`type`
        localEnv = localEnv.next
      }
      undefinedVar(pos, id)
    case Let(pos, defs, exp) =>
      var local = env
      for(d <- defs) {
        local = put(local, d.id, doType(d.exp, env))
      }
      doType(exp, local)
    case App(pos, fn, arg) =>
      val argType = doType(arg, env)
      if(argType == ERROR) return ERROR
      val funType = doType(fn, env)
      if(funType.isInstanceOf[FUNCTION_TYPE] && funType.asInstanceOf[FUNCTION_TYPE].arg.equals(argType)){
        return funType.asInstanceOf[FUNCTION_TYPE].ret
      }else{
        errorFun(pos, funType, fun(argType, `var`("a")))
      }
    case Return(pos, exp) =>
      val lt = doType(exp, env)
      if(lt == ERROR) return ERROR
      io(lt)
    case AnonFun(pos, arg, exp) =>
      val local = env.updated(arg.id, arg.tpe)
      val ret = doType(exp, local)
      if(ret == ERROR) return ERROR
      fun(arg.tpe, ret)
    case And(pos, lhs, rhs) =>
      val lt = doType(lhs, env)
      val rt = doType(rhs, env)
      if(lt == BOOL && rt == BOOL) {
        BOOL
      }else if(lt != ERROR && rt != ERROR) {
        errorBinOp(pos, BOOL, BOOL, lt, rt);
      } else {
         ERROR
      }
    case Or(pos, lhs, rhs) =>
      val lt = doType(lhs, env)
      val rt = doType(rhs, env)
      if(lt == BOOL && rt == BOOL) {
        BOOL
      }else if(lt != ERROR && rt != ERROR) {
        errorBinOp(pos, BOOL, BOOL, lt, rt);
      } else {
        ERROR
      }
    case Concat(pos, lhs, rhs) =>
      val lt = doType(lhs, env)
      val rt = doType(rhs, env)
      if(lt == ERROR) return ERROR
      if(!lt.isInstanceOf[IO_TYPE]) {
        return incompatible(lhs.pos, io(`var`("a")), lt)
      }
      if(!rt.isInstanceOf[IO_TYPE]) {
        return incompatible(rhs.pos, io(`var`("a")), rt)
      }
      rt
    case Bind(pos, lhs, rhs) =>
      val lt = doType(lhs, env)
      val rt = doType(rhs, env)
      if(lt == ERROR) return ERROR
      if(!lt.isInstanceOf[IO_TYPE]) {
        return incompatible(lhs.pos, io(`var`("a")), lt)
      }
      val genType = lt.asInstanceOf[IO_TYPE].tpe
      if(!(rt.isInstanceOf[FUNCTION_TYPE]) || rt.asInstanceOf[FUNCTION_TYPE].arg != genType){
        incompatible(rhs.pos, fun(genType, `var`("a")), rt)
      } else {
        rt.asInstanceOf[FUNCTION_TYPE].ret
      }
    case Equal(pos, lhs, rhs) =>
      val lt = doType(lhs, env)
      val rt = doType(rhs, env)
      if(lt == rt && lt != ERROR && rt != ERROR) {
        BOOL
      }else if(lt != ERROR && rt != ERROR) {
        errorBinOp(pos, lt, lt, lt, rt);
      }else {
        ERROR
      }
    case NotEqual(pos, lhs, rhs) =>
      val lt = doType(lhs, env)
      val rt = doType(rhs, env)
      if(lt == rt && lt != ERROR && rt != ERROR) {
        BOOL
      }else if(lt != ERROR && rt != ERROR) {
        errorBinOp(pos, lt, lt, lt, rt);
      }else {
        ERROR
      }
  }

  private def errorFun(pos: Pos, funType: Type, actType: Type): Type = {
    error(pos, "required " + funType + ", but " + actType)
  }

  private def errorBinOp(pos: Pos, reqLhs: Type, reqRhs: Type, actLhs: Type, actRhs: Type): Type = {
    error(pos, "required (" + reqLhs + ", " + reqRhs + "), but (" + actLhs + ", " + actRhs + ")" )
  }

  private def undefinedVar(pos: Pos, id: String): Type = {
    return error(pos, "undefined variable " + id)
  }

  private def incompatible(pos: Pos, required: Type, actual: Type): Type = {
    error(pos, "required " + required + ", but " + actual)
  }

  private def error(pos: Pos, message: String): Type = {
    printf("%d,%d: %s%n", pos.row, pos.col, message)
    ERROR
  }
}
