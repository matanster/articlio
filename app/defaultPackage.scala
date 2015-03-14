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
import com.articlio.semantic.AppActorSystem
import com.articlio.logger._
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._

object Global extends GlobalSettings {

  val db = com.articlio.storage.slickDb.db // start the slick database connection, don't wait for first use
  
  play.api.Logger.info("Global object started")
  println("Global object started")
  
  SelfMonitor
  
  //AppActorSystem.outDB ! "createIfNeeded"
  
  override def onStart(app: Application) {
    println("Global object starting non-Play stuff...")
    play.api.Logger.info("Global object starting non-Play stuff...")
    val logger = com.articlio.logger.LogManager
  }  

  override def onStop(app: Application) {
    println("Global object stopping non-Play stuff...")
    play.api.Logger.info("Global object stopping non-Play stuff...")
    AppActorSystem.shutdown
    SelfMonitor.shutdown
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