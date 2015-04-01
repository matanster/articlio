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
object Run  extends MaybeRun 
object Skip extends MaybeRun

class TestSpec (val given: String, val should: String, func: => Future[Unit], val maybeRun: MaybeRun = Run) { def attempt = func }
trait TestContainer { def tests: Seq[TestSpec] } 
trait Testable      { def TestContainer: TestContainer }

object FutureAdditions {
  implicit class FutureAdditions[T](future: Future[T]) {
    import scala.concurrent.ExecutionContext
    import scala.concurrent._
    def reverse[S](implicit executor: ExecutionContext): Future[Unit] = {
      println("in reverse")
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
    
  //
  // Dispatch all tests, list all results once they are over
  //
  def go: Unit = {
    println("running tests...")
                                                 
    //val a: Future[Int] = controllers.BulkImportRaw.TestContainer.failWithNonExistentLocation map {_ => 3}
    
    val testablesResults: Seq[Seq[Future[MaybeRun]]] = testContainers.map(testable => testable.tests
                                                                 .map(test => test.maybeRun match {
                                                                   case Run  => val a: Future[MaybeRun] = test.attempt map {_ => Run }; a
                                                                   case Skip => Future.successful(Skip)
                                                                 }))
    
    waitAll(testablesResults.flatten).map { _ => 
      // once complete, list the results
      val zipped = testablesResults zip testContainers
      (testablesResults zip testContainers).map { case(testableResults, testable) =>
        println(Console.BOLD + s"--------- ${testable.getClass.getName.dropRight(15)}" + Console.RESET)
        testableResults zip testable.tests map { case(result, testSpec) =>
          assert(result.isCompleted) 
          val TestDesc = s"Given ${testSpec.given} => should ${testSpec.should}"
          println(result.value match {
              case Some(Success(Run))  => Console.GREEN + Console.BOLD +   "[Ok]      " + Console.RESET + s"$TestDesc" 
              case Some(Success(Skip)) => Console.YELLOW_B + Console.BOLD + "[Skipped]" + Console.RESET + " " + s"$TestDesc"
              case Some(Failure(t))    => Console.RED + Console.BOLD +     "[Failed]  " + Console.RESET + s"$TestDesc [${t.toString.take(700)}${if (t.toString.length > 700) "...." else "]"}" 
              case _ => Console.RED + s"[UnitTestsRunner internal error:] test ${testSpec.attempt} not complete: ${result.isCompleted}" + Console.RESET
          })
        }
      }
    println("...tests done")
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


