package com.articlio

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
import scala.util.{Success, Failure}

object nodejsControl {
  
  //
  // is node.js responding?
  //
  def isUp: Future[Boolean] = {
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    WS.url(s"${config.nodejsServerUrl}/").withRequestTimeout(6000).get.map { response => 
      response.body match {
        case "this is the articlio node.js service..." => true
        case _ => false
      }}.recover { case e: Exception => false }
    }

  //
  // start node.js on a thread (via a future), if it isn't already responding
  //
  def startIfDown = {
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    import sys.process._ // for OS commands
    import java.io.File

    isUp map { _ match {
      case true  => println("node.js service is already up")
      case false => {
        //println("starting the node.js service...")
        val startParam = s"""--dataFilesRoot=${com.articlio.Globals.dataFilesRoot}"""
        println(s"passing node.js start param: $startParam")
        val nodejsStarter = Future { (Process(Seq("./" + config.config.getString("http-services.pdf-sourceExtractor.startScript"), startParam), 
                                             new File(config.config.getString("http-services.pdf-sourceExtractor.startDirectory"))) #>> new File("../logs/nodejs.out")).! } 
                                             .onComplete {
                                               case Failure(f) => println(s"failed starting the node.js service: $f")
                                               case Success(exitCode) => println(s"the node.js service exited with code $exitCode")
                                             }

        // need to ultimately poll or await ready message from node.js via http or stdout, before allowing actions that rely on it. deferred. 
      }
    }}
  }
  
}