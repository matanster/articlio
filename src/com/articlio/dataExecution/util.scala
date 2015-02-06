package com.articlio.dataExecution

package object util {
  
  def filePathExists(filePath: String) : Boolean = {
    import java.nio.file.{Paths, Files}
    Files.exists(Paths.get(filePath))
  }
  
  // non-useful helper function 
  def orderlyTry[T](func: => Option[T]): Option[T] = {
    try {
      return func
    } catch { case _ : Throwable => return None}
  }
  
}

