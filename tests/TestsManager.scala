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

class TestSpec(val given: String, val should: String, func: => Future[Unit]) { def attempt = func }
trait UnitTestable { def tests: Seq[TestSpec] } 

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
  
  val testables: Seq[UnitTestable] = Seq(controllers.showExtract)

  def lift(futures: Seq[Future[Unit]]): Seq[Future[Try[Unit]]] = 
    futures.map(_.map { Success(_) }.recover { case t => Failure(t) }
  )
  
  def waitAll(futures: Seq[Future[Unit]]): Future[Seq[Try[Unit]]] =
    Future.sequence(lift(futures))
  
  //
  // Dispatch all tests, list all results once they are over
  //
  def go: Unit = {
    println("running tests...")
    val testablesResults: Seq[Seq[Future[Unit]]] = testables.map(testable => testable.tests.map(test => test.attempt))
    
    // wait for all tests to complete
    val flat: Seq[Future[Unit]] = testablesResults.flatten
    
    waitAll(flat).map { _ => 

      // once complete, list the results
      val zipped: Seq[(Seq[Future[Unit]], UnitTestable)] = testablesResults zip testables
      (testablesResults zip testables).map { case(testableResults, testable) =>
        println(Console.BOLD + s"--- testable ${testable.getClass.getName} ---" + Console.RESET)
        testableResults zip testable.tests map { case(result, test) =>
          println(result.isCompleted) 
            val TestDesc = s"Given ${test.given}, should ${test.should}"
            println(result.value match {
                case Some(Success(_)) => Console.GREEN + Console.BOLD + "[Ok]: " + s"$TestDesc" + Console.RESET 
                case Some(Failure(t)) => Console.RED + Console.BOLD + "[Failed]: " + Console.RESET + s"$TestDesc [${t.toString.take(70)}${if (t.toString.length > 70) "..."}]" 
                case None => Console.RED + s"[UnitTestsRunner internal error:] test ${test.attempt} not complete: ${result.isCompleted}" + Console.RESET
            })
        }
      }
    //println("tests done")
    }
  }
}

import scala.concurrent._
import ExecutionContext.Implicits.global
import scala.util.{Success, Failure}
import scala.concurrent.Awaitable

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


