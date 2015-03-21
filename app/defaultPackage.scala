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


object Global extends GlobalSettings {

  play.api.Logger.info("Global object started")
  println("Global object started")

  //val db = com.articlio.storage.slickDb.db // start the slick database connection, don't wait for first use
  appActorSystem.outDB ! "createIfNeeded"
  
  
  //SelfMonitor
  
  override def onStart(app: Application) {
    println("Global object starting non-Play stuff...")
    play.api.Logger.info("Global object starting non-Play stuff...")
    nodejsControl.startIfDown
    val logger = com.articlio.logger.LogManager
  }  

  override def onStop(app: Application) {
    println("Global object stopping non-Play stuff...")
    play.api.Logger.info("Global object stopping non-Play stuff...")
    appActorSystem.shutdown
    //SelfMonitor.shutdown

    // TODO: is it necessary to close connection pools to keep the database happy? or does that already happen.... 
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