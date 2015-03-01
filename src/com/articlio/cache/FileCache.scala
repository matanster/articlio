package com.articlio.cache

import com.basho.riak.client.api.cap.Quorum

trait ExceptionDetail {
  def getExceptionDetail(exception: Throwable) {
    println(s"exception message: ${exception.getMessage}")
    println(s"exception cause  : ${exception.getCause}")
    println(s"exception class  : ${exception.getClass}")
    println(s"exception stack trace:\n ${exception.getStackTrace.toList.mkString("\n")}")
  }
}



class FileCache {
  
  private def filePathLocallyExists(filePath: String) : Boolean = {
    import java.nio.file.{Paths, Files}
    Files.exists(Paths.get(filePath))
  }

  def getFile(path: String) : Boolean = {
    filePathLocallyExists(path) match {
      case true  => true
      case false => true 
    }
  }
}