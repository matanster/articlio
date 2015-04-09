package com.articlio.util

object util {
    def pairIterator(s: Seq[String], f: (String, String) => Unit) {
      for (i <- 0 to s.length-2) {
        f(s(i), s(i+1))
      }
    }
}
