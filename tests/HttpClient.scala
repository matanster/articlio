package com.articlio.test


object HttpClient {

  import scala.concurrent.Future
  import scala.concurrent.Await
  import scala.concurrent.duration._
  
  import akka.actor.ActorSystem
  import akka.util.Timeout
  import akka.pattern.ask
  import akka.io.IO
  
  import spray.can.Http
  import spray.http._
  import HttpMethods._
  import spray.httpx.RequestBuilding._
  
  import scala.util.{Success, Failure}
  
  val rootTargetUrl = com.articlio.config.ownRootUrl
  
  //import system.dispatcher // implicit default execution context
  implicit val system: ActorSystem = ActorSystem("tests-actor-system")
  implicit val timeout: Timeout    = Timeout(15.seconds) // default timeout
 
  def get(request: String)(implicit timeout: Timeout) : Future[HttpResponse] = {
    val url = s"$rootTargetUrl/$request"
    println(s"attempting $url")
    (IO(Http) ? Get(url)).mapTo[HttpResponse]
  }
   
  def awaitedGet(request: String)(implicit timeout: Timeout = timeout) = {
    val httpResponse = Await.result(get(request)(timeout), Duration.Inf)
    println(httpResponse.toString)
  } 
}