package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import play.api.http.MimeTypes

trait Match {
  type Match = (String, String, String, String, String, String, Boolean, String)

  // slick class binding definition
  class Matches(tag: Tag) extends Table[Match](tag, "Matches") {
    def docName = column[String]("docName")
    def runID = column[String]("runID")
    def sentence = column[String]("sentence", O.Length(20000,varying=true), O.DBType("binary"))
    def matchPattern = column[String]("matchPattern")
    def locationActual = column[String]("locationActual")
    def locationTest = column[String]("locationTest")    
    def isFinalMatch = column[Boolean]("fullMatch")
    def matchIndication = column[String]("matchIndication")
    def * = (runID, docName, sentence, matchPattern, locationTest, locationActual, isFinalMatch, matchIndication)
  }
  
  // the table handle
  val matches = TableQuery[Matches]
}

object Application extends Controller with Match with slickGenerated.Tables {

  def playground = Action { implicit request =>
    Ok(s"nothing here...")  
  }
  
  def index = Action { implicit request =>
    println()
    Ok(s"app is up, got request [$request]")
  }
 
  def showExtractFoundation(runID: String, article: String) = DBAction { implicit request =>
  import play.api.libs.json._
  val rows = matches.filter(_.runID === runID).filter(_.docName === s"${article}.xml").list
  val content: List[Match1] = rows.map(r => Match1(r._1, r._2, r._3, r._4, r._5, r._6, r._7, r._8)).filter(_.isFinalMatch)
  //println(content)
  //val runIDs = List("ubuntu-2014-12-15 21:09:52.072", "ubuntu-2014-12-21 09:04:30.084")
  
  //val runIDs = Runids.map(r => r.runid.get).list // this would work when that table means anything
  val runIDs = matches.map(m => m.runID).list.distinct.sorted(Ordering[String].reverse)
  
  Ok(views.html.showExtractFoundation(runIDs, runID, article, content))
  //Ok(views.html.showExtractFoundation(Json.toJson(runIDs), runID, article, content))
  }
  
  def showExtract(runID: String, article: String) = DBAction { implicit request =>
    import play.api.libs.json._
    val rows = matches.filter(_.runID === runID).filter(_.docName === s"${article}.xml").list
    val content: List[Match1] = rows.map(r => Match1(r._1, r._2, r._3, r._4, r._5, r._6, r._7, r._8)).filter(_.isFinalMatch)
    //println(content)
    //val runIDs = List("ubuntu-2014-12-15 21:09:52.072", "ubuntu-2014-12-21 09:04:30.084")
    
    //val runIDs = Runids.map(r => r.runid.get).list // this would work when that table means anything
    val runIDs = matches.map(m => m.runID).list.distinct.sorted(Ordering[String].reverse)
    
    println(article)
    println(rows)
    println(runID)
    println(content)
    Ok(views.html.showExtract(runIDs, runID, article, content))
    //Ok(views.html.showExtractFoundation(Json.toJson(runIDs), runID, article, content))
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