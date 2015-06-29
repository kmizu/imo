package com.github.kmizu.imo

sealed abstract class Type
case object INT_TYPE extends Type {
  override def toString: String = "int"
}
case object STRING_TYPE extends Type {
  override def toString: String = "string"
}
case object BOOL_TYPE extends Type {
  override def toString: String = "bool"
}
case object UNIT_TYPE extends Type {
  override def toString: String = "unit"
}
case object ERROR_TYPE extends Type {
  override def toString: String = "error"
}
case class TYPE_VARIABLE(id: Symbol) {
  override def toString: String = s"'${id}"
}
case class IO_TYPE(tpe: Type) extends Type {
  override def toString: String = s"io(${tpe})"
}
case class FUNCTION_TYPE(arg: Type, ret: Type) extends Type {
  override def toString: String = s"(${arg} -> ${ret})"
}
