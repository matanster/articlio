package com.articlio.logger
import com.articlio.test._
import scala.concurrent.Future

abstract class logTag
object Performance extends logTag
object Default     extends logTag

abstract class Console
object ConsoleMirror extends Console

abstract class TagFilter { 
  def apply(msgTags: Seq[logTag]) : Seq[logTag]
}

object VoidTagFilter extends TagFilter {
  def apply(msgTags: Seq[logTag]) = Seq(Default)
}

abstract class ExclusiveTagFilter(excludeTags: Seq[logTag]) extends TagFilter { 
  def apply(msgTags: Seq[logTag]) = excludeTags.filterNot(tag => msgTags.exists(_ == tag))
} 

abstract class inclusiveTagFilter(includeTags: Seq[logTag]) extends TagFilter {
  def apply(msgTags: Seq[logTag]) = includeTags.filter(tag => msgTags.exists(_ == tag)) 
} 

abstract class Expander {
  def apply(msg: String, msgTags: Seq[logTag]) : String
}

object DefaultExpander extends Expander {
  def apply(msg: String, tags: Seq[logTag]) : String = {
    tags.map(tag => "(" + tag.getClass.getSimpleName.dropRight(1) + ")").mkString(" ") + " " + msg
  }
}

abstract class UnderlyingExternalLogger {
  def apply(finalMessage: String) 
}

object DefaultUnderlyingExternalLogger extends UnderlyingExternalLogger {
  //import org.slf4j._
  //import org.slf4j.LoggerFactory._
  val externalLogger = {
    org.slf4j.LoggerFactory.getLogger("application")
  }

  def apply(finalMessage: String) = {
    externalLogger.info(finalMessage)
  }
} 

case class Logger(tagFilter: TagFilter, expander: Expander, underlyingExternalLogger: UnderlyingExternalLogger) {
  
  def log(msg: String, tags: Seq[logTag] = Seq(Default), console: Option[Console] = None) = {

    val commonTags = tagFilter(tags)
    commonTags.nonEmpty match {
      case true =>
        DefaultUnderlyingExternalLogger(expander(msg, commonTags))
        console match {
          case Some(ConsoleMirror) => Console.print(msg)
          case Some(_) =>
          case None =>
        }        
        true
      case false => false
    }
  }
}

object LoggerTest extends Testable {
  object TestContainer extends TestContainer {
    def tests = Seq(new TestSpec(given =  "a logger without filters and a message to log",
                                 should = "log that message",
                                 logWithoutFilter, Only))
    def logWithoutFilter: Future[Unit] = {
      
      object DummyTag extends logTag
      
      val logger = Logger(VoidTagFilter, DefaultExpander, DefaultUnderlyingExternalLogger)
      logger.log("logging test log message") match {
        case true  => Future.successful(Unit)
        case false => Future.failed(new Throwable("didn't log any message"))
      }
    }                               
  }
}

