package com.articlio.storage

object ManagedDataFiles {
  
  implicit class Rooted(path: String) { // implicit class = implicitly creates an instance of itself when applied over its input type 
    def rooted = com.articlio.Globals.dataFilesRoot + path
  }
}

/*
    implicit class MyStringOps(val s: String) {  
      def endsWithAny(any: Seq[String]) : Boolean = {
        for (word <- any)
          if (s.endsWith(SPACE + word)) return true
        return false
      }
    }
*/