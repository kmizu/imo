/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Kota Mizushima, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package jp.gr.java_conf.mizu.imo;
import static jp.gr.java_conf.mizu.imo.Ast.*;

import java.util.Collections;
import java.util.List;

import javax.management.modelmbean.RequiredModelMBean;

import jp.gr.java_conf.mizu.imo.Ast.And;
import jp.gr.java_conf.mizu.imo.Ast.App;
import jp.gr.java_conf.mizu.imo.Ast.Arg;
import jp.gr.java_conf.mizu.imo.Ast.Bool;
import jp.gr.java_conf.mizu.imo.Ast.Def;
import jp.gr.java_conf.mizu.imo.Ast.Fun;
import jp.gr.java_conf.mizu.imo.Ast.If;
import jp.gr.java_conf.mizu.imo.Ast.Int;
import jp.gr.java_conf.mizu.imo.Ast.Let;
import jp.gr.java_conf.mizu.imo.Ast.Or;
import jp.gr.java_conf.mizu.imo.Ast.Program;
import jp.gr.java_conf.mizu.imo.Ast.Ref;
import jp.gr.java_conf.mizu.imo.Ast.Str;
import jp.gr.java_conf.mizu.imo.Ast.Type;
import jp.gr.java_conf.mizu.imo.Ast.Unit;
import static jp.gr.java_conf.mizu.imo.Ast.Type.*;

public class TypeChecker extends Ast.Visitor<Ast.Type, TypeChecker.Env>{
  public static class Env {
    public final Sym id;
    public final Type type;    
    public final Env next;
    public Env(Sym id, Type type, Env next) {
      this.id = id;
      this.type = type;
      this.next = next;
    }
  }
  
  public boolean typeCheck(Program node) {
    Env env = builtinEnv();
    boolean errorFound = false;
    for(Fun fun : node.funs){
      List<Arg> args = fun.args;
      Type type = fun.ret;
      for(int i = args.size() - 1; i >= 0; i--){
        type = Type.fun(args.get(i).type, type);
      }
      env = new Env(fun.id, type, env);
    }
    for(Fun fun : node.funs){
      List<Arg> args = fun.args;
      Env local = env;
      for(int i = args.size() - 1; i >= 0; i--){
        Arg arg = args.get(i);
        local = new Env(arg.id, arg.type, local);
      }
      Type type = fun.exp.accept(this, local);      
      if(type == Type.ERROR) errorFound = true;
      if(!type.equals(fun.ret)){
        incompatible(fun.pos, fun.ret, type);
        errorFound = true;
      }
    }
    return !errorFound;
  }
  
  private Env put(Env env, Sym id, Type type) {
    return new Env(id, type, env);
  }
  
  private Env put(Env env, String id, Type type) {
    return put(env, sym(id), type);
  }
  
  private Env builtinEnv() {
    Env env = null;
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
    return env;
  }

  @Override
  protected Type visit(AnonFun node, Env env) {
    env = new Env(node.arg.id, node.arg.type, env);
    Type ret = node.exp.accept(this, env);
    return ret == Type.ERROR ? Type.ERROR :
                                Type.fun(node.arg.type, ret);
  }
  
  @Override
  protected Type visit(And node, Env env) {
    Type lt = node.lhs.accept(this, env);
    Type rt = node.rhs.accept(this, env);
    if(lt == Type.BOOL && rt == Type.BOOL) {
      return Type.BOOL;
    }else if(lt != Type.ERROR && rt != Type.ERROR) {
      return errorBinOp(node.pos, Type.BOOL, Type.BOOL, lt, rt);
    }else {
      return Type.ERROR;
    }
  }
  
  @Override
  protected Type visit(Or node, Env env) {
    Type lt = node.lhs.accept(this, env);
    Type rt = node.rhs.accept(this, env);
    if(lt == Type.BOOL && rt == Type.BOOL) {
      return Type.BOOL;
    }else if(lt != Type.ERROR && rt != Type.ERROR) {
      return errorBinOp(node.pos, Type.BOOL, Type.BOOL, lt, rt);
    }else {
      return Type.ERROR;
    }
  }
  
  @Override
  protected Type visit(Bind node, Env env) {
    Type lt = node.lhs.accept(this, env);
    Type rt = node.rhs.accept(this, env);
    if(lt == Type.ERROR) return Type.ERROR;
    if(!(lt instanceof IoType)) {
        return incompatible(node.lhs.pos, io(var(sym("a"))), lt);
    }
    Type genType = ((IoType)lt).type;
    if(!(rt instanceof FunType) || !((FunType)rt).arg.equals(genType)){
      return incompatible(node.rhs.pos, fun(genType, var(sym("a"))), rt);
    }
    return ((FunType)rt).ret;
  }
  
  @Override
  protected Type visit(Concat node, Env env) {
    Type lt = node.lhs.accept(this, env);
    Type rt = node.rhs.accept(this, env);
    if(lt == Type.ERROR) return Type.ERROR;
    if(!(lt instanceof IoType)) {
        return incompatible(node.lhs.pos, io(var(sym("a"))), lt);
    }
    if(!(rt instanceof IoType)) {
      return incompatible(node.rhs.pos, io(var(sym("a"))), rt);
    }
    return rt;
  }
  
  @Override
  protected Type visit(Return node, Env env) {
    Type lt = node.exp.accept(this, env);
    if(lt == Type.ERROR) return Type.ERROR;
    return io(lt);
  }
 
  @Override
  protected Type visit(Equal node, Env env) {
    Type lt = node.lhs.accept(this, env);
    Type rt = node.rhs.accept(this, env);
    if(lt.equals(rt) && lt != Type.ERROR && rt != Type.ERROR) {
      return Type.BOOL;
    }else if(lt != Type.ERROR && rt != Type.ERROR) {
      return errorBinOp(node.pos, lt, lt, lt, rt);
    }else {
      return Type.ERROR;
    }
  }
  
  @Override
  protected Type visit(NotEqual node, Env env) {
    Type lt = node.lhs.accept(this, env);
    Type rt = node.rhs.accept(this, env);
    if(lt.equals(rt) && lt != Type.ERROR && rt != Type.ERROR) {
      return Type.BOOL;
    }else if(lt != Type.ERROR && rt != Type.ERROR) {
      return errorBinOp(node.pos, lt, lt, lt, rt);
    }else {
      return Type.ERROR;
    }
  }

  @Override
  protected Type visit(App node, Env env) {
    Type argType = node.arg.accept(this, env);
    if(argType == Type.ERROR) return Type.ERROR;
    Type funType = node.fun.accept(this, env);
    if(funType instanceof FunType && ((FunType)funType).arg.equals(argType)){
      return ((FunType)funType).ret;
    }else{
      return errorFun(
        node.pos, funType, Type.fun(argType, Type.var(sym("a")))
      );
    }
  }

  @Override
  protected Type visit(Bool node, Env env) {
    return Type.BOOL;
  }

  @Override
  protected Type visit(If node, Env env) {
    boolean errorFound = false;
    Type condType = node.cond.accept(this, env);
    if(condType != Type.BOOL && condType != Type.ERROR){
      incompatible(node.cond.pos, Type.BOOL, condType);
      errorFound = true;
    }
    Type lhsType = node.lhs.accept(this, env);
    Type rhsType = node.rhs.accept(this, env);
    if(lhsType != Type.ERROR && rhsType != Type.ERROR &&
      !lhsType.equals(rhsType)){
      errorBinOp(node.pos, lhsType, lhsType, lhsType, rhsType);
      errorFound = true;
    }
    return errorFound ? Type.ERROR : lhsType;
  }

  @Override
  protected Type visit(Int node, Env env) {
    return Type.INT;
  }

  @Override
  protected Type visit(Let node, Env env) {
    Env local = env;
    for(Def def : node.defs){
      local = put(local, def.id, def.exp.accept(this, env));
    }
    return node.exp.accept(this, local);
  }
  
  @Override
  protected Type visit(Ref node, Env env) {
    while(env != null) {
      if(node.id == env.id) return env.type;
      env = env.next;
    }
    return undefinedVar(node.pos, node.id);
  }

  @Override
  protected Type visit(Str node, Env env) {
    return Type.STRING;
  }

  @Override
  protected Type visit(Unit node, Env env) {
    return Type.UNIT;
  }
  
  private Type errorFun(Pos pos, Type funType, Type actType) {
    return error(pos, 
      "required " + funType + ", but " + actType
    );
  }
  
  private Type errorBinOp(
    Pos pos, Type reqLhs, Type reqRhs, Type actLhs, Type actRhs
  ) {
    return error(pos, 
      "required (" + reqLhs + ", " + reqRhs + "), but (" + actLhs + ", " + actRhs + ")"
    );
  }
  
  private Type undefinedVar(Pos pos, Sym id) {
    return error(pos, "undefined variable " + id);
  }
  
  private Type incompatible(Pos pos, Type required, Type actual) {
    return error(pos, "required " + required + ", but " + actual);
  }
  
  private Type error(Pos pos, String message) {
    System.out.printf("%d,%d: %s%n", pos.row, pos.col, message);
    return Type.ERROR;
  }
}
