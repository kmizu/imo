/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Kota Mizushima, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package jp.gr.java_conf.mizu.imo;

import static jp.gr.java_conf.mizu.imo.Ast.Type.BOOL;
import static jp.gr.java_conf.mizu.imo.Ast.Type.INT;
import static jp.gr.java_conf.mizu.imo.Ast.Type.fun;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import jp.gr.java_conf.mizu.imo.Ast.And;
import jp.gr.java_conf.mizu.imo.Ast.AnonFun;
import jp.gr.java_conf.mizu.imo.Ast.App;
import jp.gr.java_conf.mizu.imo.Ast.Arg;
import jp.gr.java_conf.mizu.imo.Ast.Bind;
import jp.gr.java_conf.mizu.imo.Ast.Bool;
import jp.gr.java_conf.mizu.imo.Ast.Concat;
import jp.gr.java_conf.mizu.imo.Ast.Def;
import jp.gr.java_conf.mizu.imo.Ast.Equal;
import jp.gr.java_conf.mizu.imo.Ast.Exp;
import jp.gr.java_conf.mizu.imo.Ast.Fun;
import jp.gr.java_conf.mizu.imo.Ast.If;
import jp.gr.java_conf.mizu.imo.Ast.Int;
import jp.gr.java_conf.mizu.imo.Ast.Let;
import jp.gr.java_conf.mizu.imo.Ast.NotEqual;
import jp.gr.java_conf.mizu.imo.Ast.Or;
import jp.gr.java_conf.mizu.imo.Ast.Program;
import jp.gr.java_conf.mizu.imo.Ast.Ref;
import jp.gr.java_conf.mizu.imo.Ast.Return;
import jp.gr.java_conf.mizu.imo.Ast.Str;
import jp.gr.java_conf.mizu.imo.Ast.Sym;
import jp.gr.java_conf.mizu.imo.Ast.Unit;

public class Evaluator extends Ast.Visitor<Object, Evaluator.Env> {
  private static final Object UNIT = new Object();
  public static class Env {
    public final Sym id;
    public Object value;
    public Env next;
    
    public Env(Sym id, Object value, Env next) {
      this.id = id;
      this.value = value;
      this.next = next;
    }
    
    public Object lookup(Sym id) {
      Env env = this;
      while(env != null) {
        if(env.id == id) return env.value;        
      }
      return null;
    }
    
    public Env put(Sym id, Object value) {
      return new Env(id, value, this);
    }
    
    public Env put(String id, Object value) {
      return new Env(Ast.sym(id), value, this);
    }
    
    @Override
    public String toString() {
      StringBuffer buf = new StringBuffer();
      Env env = this;
      buf.append("[");
      while(env != null) {
        buf.append("(" + env.id + " = " + env.value + ")");
      }
      buf.append("]");
      return new String(buf);
    }
  }
  
  public static interface NativeFunction {
    Object apply(Object arg);
  }
  
  public static class UserFunction {
    public final List<Sym> args;
    public final Exp exp;
    public Env env;

    public UserFunction(List<Sym> args, Exp exp, Env env) {
      this.args = args;
      this.exp = exp;
      this.env = env;
    }
  }
  
  public static interface Action {
    Object perform();
  }

  private Scanner scanner;
  public Evaluator() {
    scanner = new Scanner(System.in);
  }
  
  private int asInt(Object value) {
    return ((Integer)value).intValue();
  }
  
  private Env builtinEnv() {
    Env env = null;
    env = new Env(Ast.sym("+"), new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) + asInt(rhs);
          }
        };
      }
    }, env);
    env = env.put("-", new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) - asInt(rhs);
          }
        };
      }
    });
    env = env.put("*", new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) * asInt(rhs);
          }
        };
      }
    });
    env = env.put("/", new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) / asInt(rhs);
          }
        };
      }
    });
    env = env.put("%", new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) % asInt(rhs);
          }
        };
      }
    });
    env = env.put("<", new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) < asInt(rhs);
          }
        };
      }
    });
    env = env.put(">", new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) > asInt(rhs);
          }
        };
      }
    });
    env = env.put("<=", new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) <= asInt(rhs);
          }
        };
      }
    });
    env = env.put(">=", new NativeFunction() {
      public Object apply(final Object lhs) {
        return new NativeFunction() {
          public Object apply(final Object rhs) {
            return asInt(lhs) >= asInt(rhs);
          }
        };
      }
    });
    env = env.put("read_line", new Action() {
      public Object perform() {
        return scanner.nextLine();
      }
    });
    env = env.put("parse_int", new NativeFunction() {
      public Object apply(final Object arg) {
        return Integer.parseInt((String)arg);
      }
    });
    env = env.put("println", new NativeFunction() {
      public Object apply(final Object arg) {
        return new Action() {
          public Object perform() {
            System.out.println(arg);
            return UNIT;
          }
        };
      }
    });
    env = env.put("print", new NativeFunction() {
      public Object apply(final Object arg) {
        return new Action() {
          public Object perform() {
            System.out.print(arg);
            return UNIT;
          }
        };
      }
    });
    env = env.put("print_number", new NativeFunction() {
      public Object apply(final Object arg) {
        return new Action() {
          public Object perform() {
            System.out.println(arg);
            return UNIT;
          }
        };
      }
    });
    return env;
  }
  
  public Object eval(Program node, String commandLine) {
    Env env = builtinEnv();
    for(Fun f : node.funs) {
      List<Sym> args = new ArrayList<Sym>();
      for(Arg arg : f.args) args.add(arg.id);
      env = env.put(f.id, new UserFunction(args, f.exp, null));
    }
    for(Env e = env; e != null; e = e.next) {
      if(e.value instanceof UserFunction) {
        ((UserFunction)e.value).env = env;
      }
    }
    UserFunction main = (UserFunction)env.lookup(Ast.sym("main"));    
    env = new Env(main.args.get(0), commandLine, env);
    Object r = main.exp.accept(this, env);
    return r instanceof Action ? ((Action)r).perform() : r;
  }
  
  @Override
  protected Object visit(And node, Env env) {
    Object lval = node.lhs.accept(this, env);
    if(((Boolean)lval).booleanValue()){
      return node.rhs.accept(this, env);
    }
    return false;
  }
  
  @Override
  protected Object visit(AnonFun node, Env env) {
    List<Sym> args = new ArrayList<Sym>();
    args.add(node.arg.id);
    return new UserFunction(args, node.exp, env);
  }

  @Override
  protected Object visit(App node, Env env) {
    Object fun = node.fun.accept(this, env);
    Object arg = node.arg.accept(this, env);
    if(fun instanceof NativeFunction){
      return ((NativeFunction)fun).apply(arg);
    }else {
      UserFunction uf = (UserFunction)fun;
      return apply(uf, arg);
    }
  }

  private Object apply(UserFunction uf, Object arg) {
    Env local = new Env(uf.args.get(0), arg, uf.env);
    if(uf.args.size() == 1) {
      return uf.exp.accept(this, local);
    }else {
      return new UserFunction(
        uf.args.subList(1, uf.args.size()), uf.exp, local
      );
    }
  }

  @Override
  protected Object visit(Bool node, Env env) {
    return node.value;
  }

  @Override
  protected Object visit(If node, Env env) {
    if(((Boolean)node.cond.accept(this, env)).booleanValue()){
      return node.lhs.accept(this, env);
    }else {
      return node.rhs.accept(this, env);
    }
  }

  @Override
  protected Object visit(Int node, Env env) {
    return node.value;
  }

  @Override
  protected Object visit(Let node, Env env) {
    Env local = env;
    for(Def def : node.defs) {
      local = local.put(def.id, def.exp.accept(this, env));
    }
    return node.exp.accept(this, local);
  }

  @Override
  protected Object visit(Or node, Env env) {
    Object lval = node.lhs.accept(this, env);
    if(((Boolean)lval).booleanValue()){
      return true;
    }
    return node.rhs.accept(this, env);
  }
  
  @Override
  protected Object visit(Bind node, Env env) {
    final Object lval = node.lhs.accept(this, env);
    final Object rval = node.rhs.accept(this, env);
    return new Action() {
      public Object perform() {
        Object arg = ((Action)lval).perform();
        return ((Action)apply((UserFunction)rval, arg)).perform();
      }
    };
  }
  
  @Override
  protected Object visit(Concat node, Env env) {
    final Object lval = node.lhs.accept(this, env);
    final Object rval = node.rhs.accept(this, env);
    return new Action() {
      public Object perform() {
        Object arg = ((Action)lval).perform();
        return ((Action)rval).perform();
      }
    };
  }
  
  @Override
  protected Object visit(Return node, Env env) {
    final Object lval = node.exp.accept(this, env);
    return new Action() {
      public Object perform() {
        return lval;
      }
    };
  }

  @Override
  protected Object visit(Ref node, Env env) {
    while(env != null) {
      if(node.id == env.id) return env.value;
      env = env.next;
    }
    System.out.println(env);
    throw new RuntimeException(
      "variable " + node.id + " not found at "+ node.pos.row + ", " + node.pos.col
    );
  }

  @Override
  protected Object visit(Str node, Env env) {
    return node.value;
  }

  @Override
  protected Object visit(Unit node, Env env) {
    return UNIT;
  }

  @Override
  protected Object visit(Equal node, Env env) {
    Object lval = node.lhs.accept(this, env);
    Object rval = node.rhs.accept(this, env);
    return lval.equals(rval);
  }

  @Override
  protected Object visit(NotEqual node, Env env) {
    Object lval = node.lhs.accept(this, env);
    Object rval = node.rhs.accept(this, env);
    return !lval.equals(rval);
  }
}
