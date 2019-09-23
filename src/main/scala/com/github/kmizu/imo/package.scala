package com.github.kmizu

package object imo {
  val INT = INT_TYPE
  val STRING = STRING_TYPE
  val BOOL = BOOL_TYPE
  val UNIT = UNIT_TYPE
  val ERROR = ERROR_TYPE
  def fun(arg: Type, ret: Type): FUNCTION_TYPE = FUNCTION_TYPE(arg, ret)
  def io(content : Type): IO_TYPE = IO_TYPE(content)
  def `var`(name: String): TYPE_VARIABLE = TYPE_VARIABLE(name)
}
