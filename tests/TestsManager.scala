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

abstract class Result
case class Success() extends Result
case class Fail() extends Result

/*
abstract class Test {
  def preps: Option[Seq[Any]] = None 
  def run: Result
}
*/

class TestSpec(val given: String, val should: String, func: => Future[Unit]) { def attempt = func }
trait UnitTestable { def tests: Seq[TestSpec] } 

object UnitTestsRunner {
  
  val testables: Seq[UnitTestable] = Seq(controllers.showExtract)
  
  def go: Unit = {
    println("running tests...")
    //val results = testables.map(testable => testable.tests.map(test => test.attempt))
    val results: Seq[Seq[Future[Unit]]] = testables.map(testable => testable.tests.map(test => test.attempt))
    val flat = results.flatten
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    Future.sequence(flat).onComplete { println }
  }
}




@deprecated("http test will fail due to play lazy initialization in dev mode", "")
class httpPurge {
  def run = {
    HttpClient.awaitedGet("purge")
    new Success
  }
}

@deprecated("http test will fail due to play lazy initialization in dev mode", "")
class playUp {
  def run = {
    println("Hello, world!")
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    WS.url(s"http://localhost:9000/").get().map { response => println("got response") }
    new Success
  }
}


