/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Kota Mizushima, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */
package jp.gr.java_conf.mizu.imo;

import java.io.FileReader;
import java.io.IOException;

import jp.gr.java_conf.mizu.imo.parser.IMoParser;
import jp.gr.java_conf.mizu.imo.parser.ParseException;

public class IMoMain {
  private static void usage(String message) {
    System.out.println(message);
  }
  
  public static void main(String[] args) throws IOException {
    if(args.length == 0) {
      usage("java jp.gr.java_conf.mizu.imo.IMoMain <file name>");
    }
    FileReader reader = new FileReader(args[0]);
    try {
      IMoParser parser = new IMoParser(reader);
      Ast.Program program = parser.program();
      TypeChecker checker = new TypeChecker();
      if(!checker.typeCheck(program)) return;
      Evaluator evaluator = new Evaluator();
      evaluator.eval(program, "Hello");
    }catch(ParseException e){
      e.printStackTrace();
    }finally {
      reader.close();
    }
  }
}
