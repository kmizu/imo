package com.github.kmizu.imo

case class TypeEnvironment(id: String, tpe: Type, next: Option[TypeEnvironment])
