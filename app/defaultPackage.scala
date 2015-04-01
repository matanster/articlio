//
// Takes care of starting up non-Play code, when Play bootstraps.
//

import play.api._
import akka.pattern.ask
import akka.util.Timeout
import akka.actor.Props
import scala.concurrent.duration._
import scala.concurrent.Await
import com.articlio.SelfMonitor
import akka.actor.ActorSystem
import com.articlio.Globals.appActorSystem
import com.articlio.logger._
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import com.articlio.config
import com.articlio.nodejsControl
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.blocking
import play.api.mvc._

object Global extends GlobalSettings {

  play.api.Logger.info("Global object started")
  println("Global object started")
  
  override def onRouteRequest(request: RequestHeader): Option[Handler] = {
    //do our own path matching first - otherwise pass it onto play.
    println(request.path)
    request.path match {
      case "/override" => println("intercepted"); Some(controllers.index.go)
      case _ => Play.maybeApplication.flatMap(_.routes.flatMap {
        router => router.handlerFor(request)
      })
  }}

  
  //SelfMonitor
  
  override def onStart(app: Application) {
    println("Global object starting non-Play stuff...")
    val logger = com.articlio.logger.LogManager
    play.api.Logger.info("Global object starting non-Play stuff...")

    appActorSystem.outDB ! "createIfNeeded"
   
    nodejsControl.startIfDown
    
    play.api.Play.current.configuration.getString("mode") match { 
      case Some("test") => {
        println(s"${Console.BOLD}${Console.GREEN}\n--- starting in test mode ---\n${Console.RESET}")
        
        // now dispatch the tests for after play has been given time to initialize (otherwise fail)
        implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
        com.articlio.test.UnitTestsRunner.go
        //Future { 
          //Thread.sleep(20000L)
        //  Await.result(Future(com.articlio.test.UnitTestsRunner.go), 30.seconds)
        //}
        //Future { blocking { Thread.sleep(1000L) } }
      }
      case _ =>
    }
  }  

  override def onStop(app: Application) {
    println("Global object stopping non-Play stuff...")
    play.api.Logger.info("Global object stopping non-Play stuff...")
    appActorSystem.shutdown
    com.articlio.Globals.db.db.close
    //SelfMonitor.shutdown
  }  
  
  
  
  
  
  
  
  
  
  
  
  
  /*
  import sorm._

  object Db extends Instance (
    entities = Set( Entity[Coffee](), 
                    Entity[Supplier]() ),
    url = s"jdbc:mysql://localhost:3306/articlio/?user=articlio?password=articlio?d"
  )
  
  case class Coffee ( name : String, supplier : Supplier, price : Double, sales : Int, total : Int )
  case class Supplier ( name : String, street : String, city : String, state : String, zip : String )
  */
}