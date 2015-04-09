/*
 *  stub code with the jcloud imports - disabled for now in build.sbt
 */

/*

package com.articlio.cache

import org.jclouds.rackspace.cloudfiles.v1._
import org.jclouds.ContextBuilder

trait ExceptionDetail {
  def getExceptionDetail(exception: Throwable) {
    println(s"exception message: ${exception.getMessage}")
    println(s"exception cause  : ${exception.getCause}")
    println(s"exception class  : ${exception.getClass}")
    println(s"exception stack trace:\n ${exception.getStackTrace.toList.mkString("\n")}")
  }
}

object CloudFiles {
  val cloudFilesApi = ContextBuilder.newBuilder("rackspace-cloudfiles-us")
    .credentials("{username}", "{apiKey}")
    .buildApi(classOf[CloudFilesApi])
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

*/