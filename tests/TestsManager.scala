package com.articlio.test

import models._
import play.api._
import play.api.mvc._
//import play.api.db.slick._ play slick plugin is not yet interoperable with Slick 3.0.0, slick is wired in without it
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import play.api.http.MimeTypes
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import scala.concurrent.Future
import com.articlio.config

abstract class Result
case class Success() extends Result
case class Fail() extends Result

abstract class Test {
  def preps: Option[Seq[Any]] = None 
  def run: Result
}

object TestsRunner {

  val tests: Seq[Test] = Seq(new Test1, 
                             new Test2)
  def go {
    tests.map(test => {
        test.run
      }
    )
  }
}

class Test1 extends Test {
  def run = {
    new Success
  }
}

class Test2 extends Test {
  def run = {
    new Success
  }
}



object EndToEnd {
  def playWS {
    println("Hello, world!")
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    WS.url(s"http://localhost:9000/").get().map { response => println("got response") }
  }
  
}


