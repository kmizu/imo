package com.github.kmizu.imo

case class Environment(id: String, value: Any, next: Option[Environment]) {
  def lookup(name: String): Option[Any] = {
    if(name == id) {
      Some(value)
    } else {
      next.flatMap{e => e.lookup(name)}
    }
  }

  def updated(newId: String, newValue: Any): Environment = {
    Environment(newId, newValue, Some(this))
  }

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

    buf.toString()
  }
}
