package controllers

import play.api._
import play.api.mvc._
import play.api.db.slick._
import models.Tables._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import play.api.http.MimeTypes
//import play.api.libs.json._

object Application extends Controller {

  def bulkImport(path: String) = Action { implicit request =>
    println("at bulk import controller")
    com.articlio.dataExecution.concrete.Importer.bulkImport(path) match {
      case true => Ok("Import successful")
      case false => Ok("Import failed")
    }
  }
  
  def showExtract(articleName: String,
                  pdb: String,
                  dataID: Option[Long]) = DBAction { implicit request =>

    import com.articlio.dataExecution._
    import com.articlio.dataExecution.concrete._
    
    def show(dataID: Long) = {
      val content = Matches.filter(_.dataid === dataID).filter(_.docname === s"${articleName}.xml").filter(_.fullmatch)
      val dataIDs = models.Tables.Data.map(m => m.dataid).list.distinct.sorted(Ordering[Long].reverse)
      val unlifted = content.asInstanceOf[List[models.Tables.MatchesRow]]
      Ok(views.html.showExtract(dataIDs, dataID, pdb, articleName, unlifted))
    }
    
    val executionManager = new DataExecutionManager

    //
    // split on whether a specific data ID was requested or not 
    //
    dataID match {
      
      // no specific run id requested
      case None => 
        executionManager.getSingleDataAccess(new SemanticData(articleName)()) match {
          case error: AccessError => 
            Ok("Result data failed to create. Please contact development with all necessary details (url, and description of what you were doing)")
          case access: Access => {
            val lastDataID = models.Tables.Data.map(m => m.dataid).list.distinct.sorted(Ordering[Long].reverse).head
            show(lastDataID)
          }
        }
      
      
      // a specific run id requested
      case Some(dataID) =>
        executionManager.getSingleDataAccess(new SemanticData(articleName)()) match {
          case error: DataIDNotFound => 
            Ok("There is no result data for the requested data ID")
          case accesss: Access => {
            Ok("There is no result data for the requested data ID")
            show(dataID)
          } 
        }
    }
  }
 
  def showOriginal(article: String) = Action { implicit request =>
    if (!article.startsWith("elife")){
      val original = s"../data/pdf/0-input/${article}.pdf" 
      println(s"serving $original")
      Ok.sendFile(content = new java.io.File(original),
                  inline = true) // as per https://www.playframework.com/documentation/2.1.3/ScalaStream
    }
    else
      Redirect(s"http://ubuntu.local:8000/${article}.xml") // assuming path ../data/eLife-JATS/2-styled is being web served
                                                           // e.g. by "python -m SimpleHTTPServer"
  }
            
  def adminPage = Action { implicit request => Ok(views.html.adminPage()) }
  
  def playground = Action { implicit request => Ok(s"nothing here...") }
  
  def index = Action { implicit request => Ok(s"app is up, got request [$request]") }
  
  /*
  import play.api.mvc._
  import play.api.Play.current
  import akka.actor._

  object MyWebSocketActor {
    def props(out: ActorRef) = Props(new MyWebSocketActor(out))
  }
  
  class MyWebSocketActor(out: ActorRef) extends Actor {
    def receive = {
      case msg: String =>
        out ! ("I received your message: " + msg)
    }
}
  def socket = WebSocket.acceptWithActor[String, String] { request => out =>
    MyWebSocketActor.props(out)
  }
*/  
}
