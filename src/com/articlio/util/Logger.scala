package com.articlio.util

import com.articlio.config
//import java.io.{File}
import java.nio.file.{Paths, Files, StandardOpenOption}
import java.nio.charset.StandardCharsets
import java.nio.file.{Path, Paths, Files}
import org.apache.commons.io.FileUtils.{deleteDirectory}

/*
 *  Semantically route text output to destinations, currently only to local file destinations
 */

class Logger(loggerName: String) {

  def managedLogs = scala.collection.mutable.Map.empty[Seq[String], java.nio.file.Path]
  val base = config.config.getString("logging")
  if (!Files.exists(Paths.get(base))) Files.createDirectory(Paths.get(base)) // create base folder if it doesn't yet exist
  
  // initialize a logging path under the logging base directory and overall logger name
  private def initialize(logicalPath:Seq[String]): java.nio.file.Path = {
    val physicalPath = s"$base/$loggerName/${logicalPath.dropRight(1).map(pathSegment => s"$pathSegment/")}${logicalPath.last}.log"
    return Paths.get(physicalPath)
  }
  
  def getLog(logicalPath: Seq[String]) = {
    managedLogs.contains(logicalPath) match {
      case true  => managedLogs(logicalPath)
      case false => {
        val initialized = initialize(logicalPath)
        managedLogs(logicalPath) = initialized
        initialized
      }
    }
  }
  
  // logs a message message - see http://docs.oracle.com/javase/7/docs/api/java/nio/file/Files.html
  // TODO: consider optimizing via the buffered writer class therein or per https://docs.oracle.com/javase/tutorial/essential/io/file.html
  def write(string: String, logicalPath:Seq[String], console: Boolean = false) = {
    val bytes = string + "\n"
    Files.write(getLog(logicalPath), bytes.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND) // buffered writing may be more performant than this... see java.nio.file...
  }
}

class DataLogger(dataType: String, dataTopic: String, dataID: Long) extends Logger("data") {
  def write(string: String) = super.write(string, Seq(dataType, dataTopic, dataID.toString))
}