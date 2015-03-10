package controllers

import play.api.{db => _, _}
import play.api.mvc._
import models.Tables._
import com.articlio.storage.slickDb
import slick.driver.MySQLDriver.api._
import play.api.http.MimeTypes
import models.Tables
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
//import slick.backend.DatabasePublisher
//import slick.driver.H2Driver.api._
//import slick.lifted.{ProvenShape, ForeignKeyQuery}

object Application extends Controller {

  def bulkImportRaw(path: String) = Action { implicit request =>
    println("at bulk import controller")
    com.articlio.dataExecution.concrete.Importer.bulkImportRaw(path) match {
      case true => Ok("Import successful")
      case false => Ok("Import failed")
    }
  }

  def showExtract(articleName: String,
                  pdb: String,
                  dataID: Option[Long]) = Action.async { implicit request =>

    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    import com.articlio.dataExecution._
    import com.articlio.dataExecution.concrete._
    
    def show(dataID: Long) = {
      val query : DBIO[Seq[MatchesRow]] = Matches.filter(_.dataid === dataID).filter(_.docname === s"${articleName}.xml").filter(_.fullmatch).result
      val content: Future[Seq[Tables.MatchesRow]] = slickDb.db.run(query)
      content.map { result => {  
          //val dataIDs = models.Tables.Data.map(m => m.dataid).list.distinct.sorted(Ordering[Long].reverse)
          val dataIDs = List(3L,4L)
          Ok(views.html.showExtract(dataIDs, dataID, pdb, articleName, result.toList))
        }
      }
      //val content = slickDb.db.run()

    }
    
    val attemptedData = AttemptDataObject(SemanticData(articleName, pdb, dataID)())

    attemptedData.accessOrError match {
      case access: Access =>      show(attemptedData.dataID.get)
      case error:  AccessError => Future { Ok(s"couldn't find or create data for request: ${attemptedData.humanAccessMessage}") }
    }
  }
  
  @deprecated("depcracated by newer", "showExtract")
  def showExtractOld(articleName: String,
                  pdb: String,
                  dataID: Option[Long]) = Action { implicit request =>

    import com.articlio.dataExecution._
    import com.articlio.dataExecution.concrete._
    
    def show(dataID: Long) = {
      val content = Matches.filter(_.dataid === dataID).filter(_.docname === s"${articleName}.xml").filter(_.fullmatch).list
      val dataIDs = models.Tables.Data.map(m => m.dataid).list.distinct.sorted(Ordering[Long].reverse)
      Ok(views.html.showExtract(dataIDs, dataID, pdb, articleName, content))
    }
    
    //
    // split on whether a specific data ID was requested or not 
    //
    dataID match {
      
      // no specific run id requested
      case None => 
        AttemptDataObject(SemanticData(articleName)()).accessOrError match {
          case error: AccessError => 
            Ok("Result data failed to create. Please contact development with all necessary details (url, and description of what you were doing)")
          case access: Access => {
            val lastDataID = models.Tables.Data.map(m => m.dataid).list.distinct.sorted(Ordering[Long].reverse).head
            show(lastDataID)
          }
        }
      
      
      // a specific run id requested
      case Some(dataID) =>
        AttemptDataObject(SemanticData(articleName)()).accessOrError match {
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
