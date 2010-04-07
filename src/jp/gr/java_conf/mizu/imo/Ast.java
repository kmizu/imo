/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2007, Kota Mizushima, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package jp.gr.java_conf.mizu.imo;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Ast {  
  public static App app(Exp fun, Exp arg) {
    return new App(fun.pos, fun, arg);
  }
  
  public static App app2(Exp fun, Exp arg1, Exp arg2) {
    return app(app(fun, arg1), arg2);
  }
  
  public static Ref ref(Pos pos, String id) {
    return new Ref(pos, Sym.intern(id));
  }
  
  public static Sym sym(String id) {
    return Sym.intern(id);
  }
  
  public static final class Sym {
    public final String value;
    private Sym(String value) {
      this.value = value;
    }
    
    public boolean equals(Object obj) {
      if(!(obj instanceof Sym)) return false;
      return value == ((Sym)obj).value;
    }
    
    private static final Map<String, Sym> TABLE = new HashMap<String, Sym>();
    
    public static Sym intern(String key) {
      Sym s = TABLE.get(key);
      if(s == null) {
        s = new Sym(key);
        TABLE.put(key, s);
      }
      return s;
    }
    
    @Override
    public String toString() {
      return value;
    }
  }
  
  public static class Pos {
    public final int row;
    public final int col;
    public Pos(int row, int col) {
      this.row = row;
      this.col = col;
    }
  }
  
  public abstract static class Type {
    public static IntType INT = IntType.INSTANCE;
    public static BoolType BOOL = BoolType.INSTANCE;
    public static UnitType UNIT = UnitType.INSTANCE;
    public static StringType STRING = StringType.INSTANCE;
    public static ErrorType ERROR = ErrorType.INSTANCE;
    public static FunType fun(Type arg, Type ret) {
      return new FunType(arg, ret);
    }
    public static TypeVar var(Sym id) {
      return new TypeVar(id);
    }
    
    public static IoType io(Type type) {
      return new IoType(type);
    }
  }
  
  public static final class IntType extends Type {
    public static final IntType INSTANCE = new IntType();
    private IntType() {}
    @Override
    public String toString() {
      return "int";
    }
  }
  
  public static final class BoolType extends Type {
    public static final BoolType INSTANCE = new BoolType();
    private BoolType() {}
    public String toString() {
      return "bool";
    }
  }
  
  public static final class UnitType extends Type {
    public static final UnitType INSTANCE = new UnitType();
    private UnitType() {}
    public String toString() {
      return "unit";
    }
  }
  
  public static final class StringType extends Type {
    public static final StringType INSTANCE = new StringType();
    private StringType() {}
    public String toString() {
      return "string";
    }
  }
  
  public static final class ErrorType extends Type {
    public static final ErrorType INSTANCE = new ErrorType();
    private ErrorType() {}
    public String toString() {
      return "error";
    }
  }
  
  public static final class FunType extends Type {
    public final Type arg;
    public final Type ret;
    public FunType(Type arg, Type ret) {
      this.arg = arg;
      this.ret = ret;
    }
    @Override
    public boolean equals(Object obj) {
      if(!(obj instanceof FunType)) return false;
      FunType type = (FunType)obj;
      return arg.equals(type.arg) && ret.equals(type.ret);
    }
    public String toString() {
      return "(" + arg + " -> " + ret + ")";
    }
  }
  
  public static final class IoType extends Type {
    public final Type type;
    public IoType(Type type) {
      this.type = type;
    }
    @Override
    public boolean equals(Object obj) {
      if(!(obj instanceof IoType)) return false;
      return type.equals(((IoType)obj).type);
    }
    public String toString() {
      return "io(" + type + ")";
    }
  }
  
  public static final class TypeVar extends Type {
    public final Sym id;
    public TypeVar(Sym id) {
      this.id = id;
    }
    public String toString() {
      return "'" + id;
    }
  }
  
  public abstract static class Node {
    public final Pos pos;
    public Node(Pos pos) {
      this.pos = pos;
    }
    public abstract <R, E> R accept(Visitor<R, E> visitor, E env);
  }
  
  public static class Program extends Node {
    public final List<Fun> funs;
    public Program(Pos pos, List<Fun> funs) {
      super(pos);
      this.funs = funs;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Def extends Node {
    public final Sym id;
    public final Exp exp;
    
    public Def(Pos pos, Sym id, Exp exp) {
      super(pos);
      this.id = id;
      this.exp = exp;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Arg extends Node {
    public final Sym id;
    public final Type type;
    
    public Arg(Pos pos, Sym id, Type type) {
      super(pos);
      this.id = id;
      this.type = type;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Fun extends Node {
    public final Sym id;
    public final List<Arg> args;
    public final Type ret;
    public final Exp exp;
    public Fun(Pos pos, Sym id, List<Arg> args, Type ret, Exp exp) { 
      super(pos);
      this.id = id;
      this.args = args;
      this.ret = ret;
      this.exp = exp;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class AnonFun extends Exp {
    public final Arg arg;
    public final Exp exp;
    public AnonFun(Pos pos, Arg arg, Exp exp) { 
      super(pos);
      this.arg = arg;
      this.exp = exp;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
    
  public abstract static class Exp extends Node {
    public Exp(Pos pos) { super(pos); }
  }
  
  public static class Ref extends Exp {
    public final Sym id;
    public Ref(Pos pos, Sym id) {
      super(pos);
      this.id = id;
    }
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class App extends Exp {
    public final Exp fun;
    public final Exp arg;
    
    public App(Pos pos, Exp fun, Exp arg) {
      super(pos);
      this.fun = fun;
      this.arg = arg;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Let extends Exp {
    public final List<Def> defs;
    public final Exp exp;
    public Let(Pos pos, List<Def> defs, Exp exp) {
      super(pos);
      this.defs = defs;
      this.exp = exp;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class If extends Exp {
    public final Exp cond;
    public final Exp lhs;
    public final Exp rhs;
    
    public If(Pos pos, Exp cond, Exp lhs, Exp rhs) {
      super(pos);
      this.cond = cond;
      this.lhs = lhs;
      this.rhs = rhs;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class And extends Exp {
    public final Exp lhs;
    public final Exp rhs;
    public And(Pos pos, Exp lhs, Exp rhs) {
      super(pos);
      this.lhs = lhs;
      this.rhs = rhs;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Bind extends Exp {
    public final Exp lhs;
    public final Exp rhs;
    public Bind(Pos pos, Exp lhs, Exp rhs) {
      super(pos);
      this.lhs = lhs;
      this.rhs = rhs;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Concat extends Exp {
    public final Exp lhs;
    public final Exp rhs;
    public Concat(Pos pos, Exp lhs, Exp rhs) {
      super(pos);
      this.lhs = lhs;
      this.rhs = rhs;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Return extends Exp {
    public final Exp exp;
    public Return(Pos pos, Exp exp) {
      super(pos);
      this.exp = exp;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Or extends Exp {
    public final Exp lhs;
    public final Exp rhs;
    public Or(Pos pos, Exp lhs, Exp rhs) {
      super(pos);
      this.lhs = lhs;
      this.rhs = rhs;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Equal extends Exp {
    public final Exp lhs;
    public final Exp rhs;
    public Equal(Pos pos, Exp lhs, Exp rhs) {
      super(pos);
      this.lhs = lhs;
      this.rhs = rhs;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class NotEqual extends Exp {
    public final Exp lhs;
    public final Exp rhs;
    public NotEqual(Pos pos, Exp lhs, Exp rhs) {
      super(pos);
      this.lhs = lhs;
      this.rhs = rhs;
    }
    
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }

  public static class Int extends Exp {
    public final int value;
    public Int(Pos pos, int value) {
      super(pos);
      this.value = value;
    }
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Str extends Exp {
    public final String value;
    public Str(Pos pos, String value) {
      super(pos);
      this.value = value;
    }
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public static class Bool extends Exp {
    public final boolean value;
    public Bool(Pos pos, boolean value) {
      super(pos);
      this.value = value;
    }
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }

  public static class Unit extends Exp {
    public Unit(Pos pos) {
      super(pos);
    }
    @Override
    public <R, E> R accept(Visitor<R, E> visitor, E env) {
      return visitor.visit(this, env);
    }
  }
  
  public abstract static class Visitor<R, E> {
    protected R visit(Arg node, E env){ return null; }
    protected R visit(App node, E env){ return null; }
    protected R visit(Bind node, E env){ return null; }
    protected R visit(Concat node, E env){ return null; }
    protected R visit(Return node, E env){ return null; }
    protected R visit(Fun node, E env){ return null; }
    protected R visit(Ref node, E env){ return null; }
    protected R visit(Def node, E env){ return null; }
    protected R visit(Let node, E env){ return null; }
    protected R visit(AnonFun node, E env){ return null; }
    protected R visit(And node, E env){ return null; }
    protected R visit(Or node, E env){ return null; }
    protected R visit(If node, E env){ return null; }
    protected R visit(Int node, E env){ return null; }
    protected R visit(Str node, E env){ return null; }
    protected R visit(Bool node, E env){ return null; }
    protected R visit(Unit node, E env){ return null; }
    protected R visit(Equal node, E env){ return null; }
    protected R visit(NotEqual node, E env){ return null; }
    protected R visit(Program node, E env){ return null; }
  }
}
