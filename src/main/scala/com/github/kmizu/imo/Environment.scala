package com.github.kmizu.imo

case class Environment(id: Symbol, value: Any, next: Option[Environment]) {
  override def toString: String = {
    val buf = new StringBuilder
    var envOpt = Option(this)
    buf.append("[")

    while (envOpt != None) {
      val env = envOpt.get
      buf.append("(" + env.id + " = " + env.value + ")")
      envOpt = env.next
    }
    buf.append("]")

    new String(buf)
  }
}
