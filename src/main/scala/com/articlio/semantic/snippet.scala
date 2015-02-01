package com.articlio.semantic;
//
// place for code experiments 
//

abstract class A
case object B extends A

object something {
  val b = B
  b match { case B => println("success") }
}