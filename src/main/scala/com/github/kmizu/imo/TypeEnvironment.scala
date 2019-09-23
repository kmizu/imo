package com.github.kmizu.imo

case class TypeEnvironment(id: String, `type`: Type, next: TypeEnvironment) {
  def updated(newId: String, newType: Type): TypeEnvironment = {
    TypeEnvironment(newId, newType, this)
  }

  override def toString: String = {
    val buf = new StringBuilder
    var env= this
    buf.append("[")

    while (env != null) {
      buf.append("(" + env.id + " = " + env.`type` + ")")
      env = env.next
    }
    buf.append("]")

    buf.toString()
  }
}
