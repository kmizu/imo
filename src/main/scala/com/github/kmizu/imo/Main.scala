package com.github.kmizu.imo

import java.nio.file.{Files, Path, Paths}
import com.github.kmizu.imo.TypeChecker._
import com.github.kmizu.imo.Parser.parse

object Main {
  def main(args: Array[String]): Unit = {
    val content = new String(Files.readAllBytes(Paths.get("example/fib.imo")), "UTF-8")
    val tree = parse(content)
    typeCheck(tree)
    Evaluator.eval(tree, "")
    println(tree)
  }
}
