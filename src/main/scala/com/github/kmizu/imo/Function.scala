package com.github.kmizu.imo

trait Function[-A, +B] {
  def apply(arg: A): B
}
