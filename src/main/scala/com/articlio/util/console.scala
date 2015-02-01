package com.articlio.util

object Console {

  import scala.Console._     // available colors and styling at: http://www.scala-lang.org/api/2.10.2/index.html#scala.Console$
  val GRAY    = "\u001b033[90m"   // and some more 
  val ITALICS = "\u001b033[3m"    // and some more 

  def log(message:String, msgType: String) {

    msgType match {
      case "performance" => println(MAGENTA + message + RESET)
      case "startup"     => println(GREEN + message + RESET)
      case "timers"      => println(message)
      case _ => // swallow the message
    }
  }
}