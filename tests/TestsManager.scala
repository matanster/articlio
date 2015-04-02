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

abstract class MaybeRun
object Run extends MaybeRun
object Skip extends MaybeRun
object Only extends MaybeRun // a bit of a hack, for allowing to quickly mark one test as the only one to run

case class BeenRun(timeRun: Long) extends MaybeRun

class TestSpec (val given: String, 
                val should: String, 
                func: => Future[Unit], 
                val maybeRun: MaybeRun = Run, 
                val timeLimit: Duration = 10.seconds) { def attempt = func }

trait TestContainer { def tests: Seq[TestSpec] } 
trait Testable      { def TestContainer: TestContainer }

object FutureAdditions {
  implicit class FutureAdditions[T](future: Future[T]) {
    import scala.concurrent.ExecutionContext
    import scala.concurrent._
    def reverse[S](implicit executor: ExecutionContext): Future[Unit] = {
      val p = Promise[Unit]()
      future.onComplete {
        // reverse the result of the future
        case Success(r) => p.failure(new Throwable(s"should not have received result (received result: $r)")) 
        case Failure(t) => p.success(Unit)              
      }
      p.future
    }
  }
}

object UnitTestsRunner {

  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  
  val testables: Seq[Testable] = Seq(controllers.ShowExtract,
                                     controllers.BulkImportRaw)
  
  val testContainers = testables.map(testable => testable.TestContainer)

  // wait for entire sequence of futures to complete, even if some of them failed (unlike the standard library's .sequence), via lifting to Try objects.
  private def waitAll[T](futures: Seq[Future[T]]): Future[Seq[Try[T]]] =
    Future.sequence(lift(futures))
  
  private def lift[T](futures: Seq[Future[T]]): Seq[Future[Try[T]]] = 
    futures.map(_.map { Success(_) }.recover { case t => Failure(t) })

  @volatile var running = false
  def go: Unit = {
    println(Console.BLUE_B + running + Console.RESET)
    running match {
      case true  => println("tests already running - request ignore")
      case false => running = true; doGo; running = false 
    }
  }
    
  //
  // Dispatch all tests, list all results once they are over
  //
  def doGo: Unit = {
    import scala.Console._ // Hopefully this doesn't bite
    val terminalWidth = jline.TerminalFactory.get().getWidth();
    
    println(BOLD + "running tests..." + RESET)

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

    def timedAttempt(test: TestSpec) = {
      val time = System.currentTimeMillis
      test.attempt map {_ => 
        val elapsedTime = System.currentTimeMillis() - time 
        BeenRun(elapsedTime)
      }
    }
    
    val testMarkedAsOnly = testContainers.map(testable => testable.tests
                                             .map(test => test.maybeRun == Only)).flatten.filter(_ == true)

    val testablesResults: Seq[Seq[Future[MaybeRun]]] = (testMarkedAsOnly.isEmpty) match {
      case true  => testContainers.map(testable => testable.tests
                                                           .map(test => test.maybeRun match {
                                                             case Run  => timedAttempt(test)
                                                             case Skip => Future.successful(Skip)
                                                           }))

      case false => {
        println(YELLOW + BOLD + """One or more tests are flagged as "Only" - running only those tests and skipping all others""" + RESET)
        testContainers.map(testable => testable.tests
                      .map(test => test.maybeRun match {
                        case Only       => timedAttempt(test)
                        case Run | Skip => Future.successful(Skip)
                      }))
      }
    }                                         

                                                                 
    waitAll(testablesResults.flatten).map { _ => 
      // once complete, list the results
      val zipped = testablesResults zip testContainers
      (testablesResults zip testContainers).map { case(testableResults, testable) =>
        println(WHITE_B + BLACK + BOLD + s"          ${testable.getClass.getName.dropRight(15)}".padTo(terminalWidth, ' ') + RESET)
        testableResults zip testable.tests map { case(result, testSpec) =>
          assert(result.isCompleted) 
          val TestDesc = s"given ${testSpec.given} <=> should ${testSpec.should}"
          println(result.value match {
              case Some(Success(BeenRun(time)))  => GREEN + BOLD +  "[Ok]      " + RESET + TestDesc + s"   ⌛ $time msec" //⌛⌚
              case Some(Success(Skip)) => YELLOW + BOLD + "[Skip]    " + RESET + TestDesc
              case Some(Failure(t))    => RED + BOLD +    "[Failed]  " + RESET + TestDesc + 
                                          RED + "\n" + lineWrapForConsole(t.getMessage) + RESET
              // case Some(Failure(t))    => RED + BOLD +     "[Failed]  " + RESET + TestDesc + RED + "\n          ∗ " + t.getMessage.take(700) + (if (t.toString.length > 700) "...." else "") + RESET
              case _ => Console.RED + s"[UnitTestsRunner internal error:] test ${testSpec.attempt} not complete: ${result.isCompleted}" + RESET
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


