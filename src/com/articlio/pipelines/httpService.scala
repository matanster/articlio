package com.articlio.pipelines.concrete
//import com.articlio.util.runID

//
// Spray imports
//
import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Actor
import spray.routing._
import spray.http._
import MediaTypes._
import scala.concurrent.duration._

class MyServiceActor extends Actor with MyService {

  // the HttpService trait defines only one abstract member, which
  // connects the services environment to the enclosing actor or test
  def actorRefFactory = context

  // this actor only runs our route, but you could add
  // other things here, like request stream processing
  // or timeout handling
  def receive = runRoute(myRoute)
}

// this trait defines our service behavior independently from the service actor
trait MyService extends HttpService {
  
  val myRoute =
    get {
	  path("") { 
  		complete {
          //<html><body><h1>{buildVersioning.Info.name} service is up</h1></body></html>
          "${buildVersioning.Info.name} service is up"
        }
    } ~
    path("filesTransformer") {
      parameter('all) { _ =>
        val eLifeJATSpipeline = new JATSpipeline
        val PDFpipeline = new ConvertedCorpusPipeline
    	  complete("Done transforming input files")
      } ~
      parameter('inputFile) { inputFile =>
        complete("Processing single input file not implemented yet")
      }
    }
  }
}

object HttpService {

  // ActorSystem for spray
  implicit val sprayActorSystem = ActorSystem("spray-actor-system")

  // create and start our service actor
  val service = sprayActorSystem.actorOf(Props[MyServiceActor], "http-service")

  implicit val timeout = Timeout(6000.seconds)
  
  // start http listener
  IO(Http) ? Http.Bind(service, interface = "localhost", port = 3333)
 
}