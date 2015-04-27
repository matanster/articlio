package com.articlio.logger
import com.articlio.test._
import scala.concurrent.Future

abstract class logTag 
object Default     extends logTag 
object Performance extends logTag
object RDBMS       extends logTag

abstract class MessageType
object Normal extends MessageType
object Error extends MessageType

abstract class TagFilter { 
  def apply(msgTags: Seq[logTag]) : Seq[logTag]
}

object VoidTagFilter extends TagFilter {
  def apply(msgTags: Seq[logTag]) = msgTags
}

class ExclusiveTagFilter(excludeTags: Seq[logTag]) extends TagFilter { 
  def apply(msgTags: Seq[logTag]) = excludeTags.filterNot(tag => msgTags.exists(_ == tag))
} 

class InclusiveTagFilter(includeTags: Seq[logTag]) extends TagFilter {
  def apply(msgTags: Seq[logTag]) = includeTags.filter(tag => msgTags.exists(_ == tag)) 
} 

abstract class Expander {
  def apply(msg: String, msgTags: Seq[logTag], messageType: MessageType) : String
}

object Util {
  def getObjectName(obj: Object) = obj.getClass.toString.split('$').last.split('.').last // obj.getClass.getSimpleName.dropRight(1)
}; import Util._

object DefaultExpander extends Expander {
  def apply(msg: String, tags: Seq[logTag], messageType: MessageType) : String = {
    import Console._
    val tagString = tags.map(tag => "[" + getObjectName(tag) + "]").mkString(" ")
    messageType match {
      case Normal => WHITE + tagString + RESET + " " + BLUE + BOLD + msg + RESET  
      case Error  => WHITE + tagString + " " + RESET + RED_B + WHITE + " Error " + RESET + " " + RED + msg + RESET // + " " + RED_B + WHITE + BOLD + " ERROR "
    }
  }
}

abstract class UnderlyingExternalLogger {
  def apply(finalMessage: String, messageType: MessageType) 
}

class SLF4J(externalLogName: String) extends UnderlyingExternalLogger {
  val externalLogger = org.slf4j.LoggerFactory.getLogger(externalLogName)

  def apply(finalMessage: String, messageType: MessageType) = messageType match {
    case Normal => externalLogger.info(finalMessage)
    case Error  => externalLogger.error(finalMessage)
  }
} 

object DefaultUnderlyingExternalLogger extends SLF4J("articlio") 

object TestDefaultUnderlyingExternalLogger extends SLF4J("test") 
  
case class Logger(tagFilter: TagFilter, 
                  expander: Expander, 
                  underlyingExternalLogger: UnderlyingExternalLogger) {
  
  def log(msg: String, console: Boolean = false, messageType: MessageType = Normal)
         (implicit tags: Seq[logTag] = Seq(Default)): Boolean = {

    import Console._
    
    val commonTags = tagFilter(tags)
    messageType match {
      case Error  => 
        underlyingExternalLogger(expander(msg, tags, messageType), messageType)
        Console.println(RED + BOLD + "Error: " +  msg + RESET)
        true
      case Normal =>
        commonTags.nonEmpty match {
          case true => 
            underlyingExternalLogger(expander(msg, commonTags, messageType), messageType)
            console match {
              case true => Console.println(msg)
              case false =>
            }        
            true
          case false => false
        }
    }
  }
}

object ActiveLogger {
  val logger = com.articlio.Globals.mode match {
    case Some("real") | None => Logger(VoidTagFilter, DefaultExpander, DefaultUnderlyingExternalLogger)
    case Some("test")        => Logger(VoidTagFilter, DefaultExpander, TestDefaultUnderlyingExternalLogger)
    case Some(other)         => throw new Throwable(s"Invalid mode parameter: $other")  
  }
}

/*
 *  CODE
 * -------
 *  TESTS 
 */

object LoggerTest extends Testable {
  object TestContainer extends TestContainer {
    
    // we don't go into the log to see what has been printed, but determine
    // each tests' result by the return of the final logging function. So
    // this is shallow testing but reasonable.
    
    def tests = Seq(new TestSpec(given =  "a logger without filters",
                                 should = "log a message",
                                 logWithoutFilter),
                    new TestSpec(given =  "a logger with an inclusive filter",
                                 should = "log a message that matches the filter",
                                 logWithInclusiveFilter1),                             
                    new TestSpec(given =  "a logger with an inclusive filter",
                                 should = "not log a message that does not match the filter",
                                 logWithInclusiveFilter2),
                    new TestSpec(given =  "a logger with an inclusive filter",
                                 should = "log a message that does not match the filter, if that message is of type Error",
                                 logWithInclusiveFilter3),
                    new TestSpec(given =  "a logger with an exclusive filter",
                                 should = "log a message that is not excluded by the filter",
                                 logWithExclusiveFilter1)          
    )
                                 
    def logWithoutFilter: Future[Unit] = {
      
      val logger = Logger(VoidTagFilter, DefaultExpander, TestDefaultUnderlyingExternalLogger)
      logger.log("logging test log message") match {
        case true  => Future.successful(Unit)
        case false => Future.failed(new Throwable("didn't log any message"))
      }
    }
    
    object TestTag  extends logTag
    object TestTag1 extends logTag
    
    def logWithInclusiveFilter1: Future[Unit] = {
      
      val tagFilter = new InclusiveTagFilter(Seq(TestTag))
      
      val logger = Logger(tagFilter, DefaultExpander, TestDefaultUnderlyingExternalLogger)
      logger.log("test log message from logWithInclusiveFilter1")(Seq(TestTag, TestTag1)) match {
        case true  => Future.successful(Unit)
        case false => Future.failed(new Throwable("didn't log message"))
      }
    }
    
    def logWithInclusiveFilter2: Future[Unit] = {
      
      val tagFilter = new InclusiveTagFilter(Seq(TestTag))
      
      val logger = Logger(tagFilter, DefaultExpander, TestDefaultUnderlyingExternalLogger)
      logger.log("test log message from logWithInclusiveFilter2")(Seq(TestTag1)) match {
        case true  => Future.failed(new Throwable("should not have logged message"))
        case false => Future.successful(Unit)
      }
    }

    def logWithInclusiveFilter3: Future[Unit] = {
      
      val tagFilter = new InclusiveTagFilter(Seq(TestTag))
      
      val logger = Logger(tagFilter, DefaultExpander, TestDefaultUnderlyingExternalLogger)
      logger.log("test error log message from logWithInclusiveFilter3", messageType = Error)(Seq(TestTag1)) match {
        case true  => Future.successful(Unit)
        case false => Future.failed(new Throwable("didn't log message"))
      }
    }
    
    def logWithExclusiveFilter1: Future[Unit] = {
      
      val tagFilter = new ExclusiveTagFilter(Seq(TestTag))
      
      val logger = Logger(tagFilter, DefaultExpander, TestDefaultUnderlyingExternalLogger)
      logger.log("test log message from logWithExclusiveFilter1")(Seq(TestTag1)) match {
        case true  => Future.successful(Unit)
        case false => Future.failed(new Throwable("didn't log message"))
      }
    }    
  }
}

