package com.articlio.logger

/*
 *  Semantically route text output to destinations, currently only to local file destinations
 *  
 *  TODO: re-factor to receive implicit context value that carries the necessary logical path
 *        for the write function, rather than instantiating different classes of writers?
 *        will that "survive" actor message passing such as in ahoCorasick.scala?
 *
 */

import com.articlio.config
import java.nio.file.{Paths, Files, StandardOpenOption}
import java.nio.charset.StandardCharsets
import java.nio.file.{Paths, Files}
import com.articlio.util.Time._

abstract class LogDirective
object ConsoleMirror extends LogDirective

//
// Provides services to instantiated loggers
//
object LogManager {

  private def managedLogs = scala.collection.mutable.Map.empty[Seq[String], java.nio.file.Path]
  
  val base = config.config.getString("logging")
  if (!Files.exists(Paths.get(base))) Files.createDirectory(Paths.get(base)) // create base folder if it doesn't yet exist
  println("articlio logger starting...")
  
  // initialize a logging path under the logging base directory and overall logger name
  private def initialize(logicalPath:Seq[String]): java.nio.file.Path = {
    val physicalPath = s"$base/${logicalPath.dropRight(1).map(pathSegment => s"$pathSegment/")}${logicalPath.last}.log"
    return Paths.get(physicalPath)
  }
  
  private def getLog(logicalPath: Seq[String]) = {
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
  def write(string: String, logicalPath:Seq[String], logDirective: Option[LogDirective]) = {
    val bytes = string + "\n"
    Files.write(getLog(logicalPath), bytes.getBytes(StandardCharsets.UTF_8), StandardOpenOption.CREATE, StandardOpenOption.APPEND) // buffered writing may be more performant than this... see java.nio.file...
    logDirective match {
      case Some(ConsoleMirror) => println("bytes")  
      case _ =>
    }
  }
  
  def shutdown = {
    println("articlio logger shutdown not yet implemented")
  }
}

//
// Loggers writing through the Log Manager object
//

abstract class Logger

class DataLogger(dataType: String, dataTopic: String) extends Logger {
  def write(string: String, logDirective: Option[LogDirective] = None) = LogManager.write(string, Seq("DataManagement", dataType, dataTopic, localNow.toString), logDirective)
}

class LdbEngineDocumentLogger(article: String) extends Logger {
  def write(string: String, outputType: String, logDirective: Option[LogDirective] = None) = LogManager.write(string, Seq(article, outputType), logDirective)
}

class SimpleLogger(loggerName: String) extends Logger {
  def write(string: String, outputType: String, logDirective: Option[LogDirective] = None) = LogManager.write(string, Seq(outputType), logDirective)
}

class SimplestLogger(loggerName: String) extends Logger {
  def write(string: String, logDirective: Option[LogDirective] = None) = LogManager.write(string, Seq(), logDirective)
}