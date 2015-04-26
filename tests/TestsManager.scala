package com.articlio.test

import models._
import play.api._
import play.api.mvc._
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import play.api.http.MimeTypes
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import scala.concurrent.Future
import com.articlio.config
import scala.util.Try
import scala.util.{Success, Failure, Try}
import scala.concurrent.Awaitable
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

abstract class testSuiteRunMode
object Parallel extends testSuiteRunMode
object Serial extends testSuiteRunMode

abstract class MaybeRun
object Run extends MaybeRun
object Skip extends MaybeRun
object Only extends MaybeRun // a bit of a hack, for allowing to quickly mark one test as the only one to run

case class BeenRun(timeRun: Long) extends MaybeRun

class TestSpec (val given: String, 
                val should: String, 
                func: => Future[Any],
                val maybeRun: MaybeRun = Run, 
                val timeLimit: Duration = 10.seconds)
                 { def attempt = { val a = func; a.onComplete { aa => println("in test manager " + aa.isSuccess)}; a } }

trait TestContainer { def tests: Seq[TestSpec] } 
trait Testable { def TestContainer: TestContainer }

object SelfTestOfTestManager extends Testable {
  object TestContainer extends TestContainer {
    def tests = Seq(new TestSpec(given = "nothing",
                                 should = "do nothing",
                                 test, Only))
    
    def test = Future.failed(new Throwable("mock failed")) map { _ => 3}
  }
}

object UnitTestsRunner {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  
  val testables: Seq[Testable] = Seq(com.articlio.logger.LoggerTest,
                                     controllers.ShowExtract,
                                     controllers.BulkImportRaw,
                                     com.articlio.dataExecution.Deduplicator,
                                     controllers.BulkFromGroup
                                    )
  
  val testContainers = testables.map(testable => testable.TestContainer)

  // wait for entire sequence of futures to complete, even if some of them failed (unlike the standard library's .sequence), via lifting to Try objects.
  private def waitAll[T](futures: Seq[Future[T]]): Future[Seq[Try[T]]] =
    Future.sequence(lift(futures))
  
  private def lift[T](futures: Seq[Future[T]]): Seq[Future[Try[T]]] = {
    println(futures)
    futures.map(_.map { Success(_) }.recover { case t => println(Console.YELLOW_B + t); Failure(t) })
  }

  @volatile var running = false // (mostly) avoid inadvertant concurrent run. a perfect way would use an atomic construct not a volatile var
  def go(suiteRunMode: testSuiteRunMode): Unit = {
    running match {
      case true  => println(Console.BLUE_B + "tests already running - request ignore" + Console.RESET)
      case false => running = true; goDo(suiteRunMode) map {_ => running = false }
    }
  }
    
  //
  // Dispatch all tests, list all results once they are over
  //
  private def goDo(suiteRunMode: testSuiteRunMode): Future[Any] = {
    import scala.Console._ // Hopefully this doesn't bite
    val terminalWidth = jline.TerminalFactory.get().getWidth();
    
    // line-wraps long string, including left indent and right margin fit
    def lineWrapForConsole(text: String) = {
      val indentLength = 10
      val rightSlack = 10
      if (terminalWidth - indentLength - rightSlack < 10) throw new Throwable("Console width too small to print tests results")

      val wrapped: List[String] = text.grouped(terminalWidth - indentLength - rightSlack).toList
      val lines = wrapped.length
      
      val indent = List.fill(indentLength)(" ").mkString
      
      (List.fill(lines)(indent) zip wrapped map { case (i, w) => i + w + "\n" }).mkString 
    } 

    val lock = new Object
    def serializedAttempt(test: TestSpec): Future[BeenRun] = lock.synchronized {
      
      println(WHITE_B + BLACK + BOLD + s"starting test $test" + Console.RESET)  // cf. https://groups.google.com/forum/#!searchin/scala-user/reflection$20function$20name/scala-user/q2L_cWxy-tE/eVWND_5Lty4J for getting the method name
      val result: Try[BeenRun] = Try(Await.result(timedAttempt(test), Duration.Inf)) // await the future, but lift it to persist future semantics also in case of exception completion
      println(WHITE_B + BLACK + BOLD + s"finished test $test - test resulted in $result" + Console.RESET)
      
      result match { // unlift from Try to regular future, as rest of this module uses plain futures for now
        case Success(s) => Future.successful(s)
        case Failure(t) => Future.failed(t)
      }
    }
    
    def timedAttempt(test: TestSpec): Future[BeenRun] = {
      val time = System.currentTimeMillis
          test.attempt map {_ => 
            val elapsedTime = System.currentTimeMillis() - time 
            BeenRun(elapsedTime)
          }
    }
    
    def attempt(test: TestSpec): Future[BeenRun] = {
      suiteRunMode match {
        case Parallel => timedAttempt(test)
        case Serial   => serializedAttempt(test)
      }
    }
    
    println(BOLD + "cleaning the database before starting tests..." + RESET)
    Await.result(com.articlio.Globals.outDB.dropCreate, Duration.Inf)
    println(BOLD + "running tests..." + RESET)
    
    val testsMarkedAsOnly = testContainers.map(testable => testable.tests
                                             .map(test => test.maybeRun == Only)).flatten.filter(_ == true)

    val testablesResults: Seq[Seq[Future[MaybeRun]]] = (testsMarkedAsOnly.isEmpty) match {
      case true  => testContainers.map(testable => testable.tests
                                                           .map(test => test.maybeRun match {
                                                             case Run  => attempt(test)
                                                             case Skip => Future.successful(Skip)
                                                           }))

      case false => {
        println(YELLOW + BOLD + """One or more tests are flagged as "Only" - running only those tests and skipping all others""" + RESET)
        testContainers.map(testable => testable.tests
                      .map(test => test.maybeRun match {
                        case Only       => attempt(test)
                        case Run | Skip => Future.successful(Skip)
                      }))
      }
    }                                         

    def getStackTraceString(t: Throwable) = { // TODO: move to util, trait, or something
      val w = new java.io.StringWriter
      val p = new java.io.PrintWriter(w)
      t.printStackTrace(p)
      w.toString
    }
    
    // once complete, list the results
    waitAll(testablesResults.flatten).map { _ => 
      val zipped = testablesResults zip testContainers
      (testablesResults zip testContainers).map { case(testableResults, testable) =>
        println(WHITE_B + BLACK + BOLD + s"          ${testable.getClass.getName.dropRight(15)}".padTo(terminalWidth, ' ') + RESET)
        testableResults zip testable.tests map { case(result, testSpec) =>
          assert(result.isCompleted) 
          val TestDesc = s"given ${testSpec.given} <=> should ${testSpec.should}"
          println(result.value match {
              case Some(Success(BeenRun(time)))  => GREEN + BOLD +  "[Ok]      " + RESET + TestDesc + s"   ⌛ $time msec" //⌛⌚
              case Some(Success(Skip))           => YELLOW + BOLD + "[Skip]    " + RESET + TestDesc
              case Some(Failure(t))              => RED + BOLD +    "[Failed]  " + RESET + TestDesc + 
                                                    RED + "\n" + lineWrapForConsole(getStackTraceString(t)) + RESET
              // case Some(Failure(t))    => RED + BOLD +     "[Failed]  " + RESET + TestDesc + RED + "\n          ∗ " + t.getMessage.take(700) + (if (t.toString.length > 700) "...." else "") + RESET
              case _ => Console.RED + s"[UnitTestsRunner internal error:] test $testSpec not complete: ${result.isCompleted}" + RESET
          })    
        }
      }
      println(BOLD + "...tests done" + RESET)
    }
  }
}





@deprecated("http test will fail due to play lazy initialization in dev mode", "")
class httpPurge {
  def run = {
    HttpClient.awaitedGet("purge")
    //new Success
  }
}

@deprecated("http test will fail due to play lazy initialization in dev mode", "")
class playUp {
  def run = {
    println("Hello, world!")
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    WS.url(s"http://localhost:9000/").get().map { response => println("got response") }
    //new Success
  }
}


