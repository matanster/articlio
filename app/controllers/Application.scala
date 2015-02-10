package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import play.api.http.MimeTypes
//import play.api.libs.json._

object Application extends Controller with Tables {

  def playground = Action { implicit request =>
    Ok(s"nothing here...")  
  }
  
  def index = Action { implicit request =>
    Ok(s"app is up, got request [$request]")
  }
 
  def showExtract(articleName: String, runID: Option[String]) = DBAction { implicit request =>

    import com.articlio.ldb
    import com.articlio.util.runID
    import com.articlio.input.JATS
    import com.articlio.config
    
    import com.articlio.dataExecution._
    import com.articlio.dataExecution.concrete._
    
    def foo(articleName: String, runID: String) = {
    
      val executionManager = new DataExecutionManager
      
      executionManager.getDataAccess(Semantic(articleName, runID)) match {
        case None =>
          Ok("Result data failed to create. Please contact development with all necessary details (url, and description of what you were doing)")
        case dataAccessDetail : Some[Access] => { 
          val content = Matches.filter(_.runid === runID).filter(_.docname === s"${articleName}.xml").filter(_.fullmatch)
          val runIDs = Matches.map(m => m.runid).list.distinct.sorted(Ordering[String].reverse)
          val unlifted = content.asInstanceOf[List[models.Tables.MatchesRow]]
          //println(unlifted.head.runid)
          Ok(views.html.showExtract(runIDs, runID, articleName, unlifted))
        }
      }
    }
    
    runID match {
      
      case None => {
        // get all run id's where this article had results. TODO: this does not distinguish between no run and a run with no line results!
        val runIDs = Matches.filter(_.docname === s"${articleName}.xml").map(m => m.runid).list.distinct.sorted(Ordering[String].reverse)
        runID.nonEmpty match {
          
          case true => {
            val latestRunID = runIDs.head
            foo(articleName, latestRunID)
          }
          case false =>
            val newRunID = (new runID).id
            foo(articleName, newRunID)
        }
        
      }
      
      case Some(runID) => foo(articleName, runID)
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
            
  def adminPage = Action { implicit request =>
    Ok(views.html.adminPage())
  }
  
  // def getRunIDs() = DBAction { implicit request =>
  //val runIDs = run
  //}

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
  
}