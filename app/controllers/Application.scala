package controllers

import play.api.{db => _, _}
import play.api.mvc._
import models.Tables._
import com.articlio.Globals.db
import slick.driver.MySQLDriver.api._
import play.api.http.MimeTypes
import models.Tables
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import com.articlio.dataExecution.FinalData
import com.articlio.dataExecution.AccessOrError
import com.articlio.dataExecution.Access
import com.articlio.config
import com.articlio.storage.ManagedDataFiles._
//import slick.backend.DatabasePublisher
//import slick.driver.H2Driver.api._
//import slick.lifted.{ProvenShape, ForeignKeyQuery}

object bulkImportRaw extends Controller {

  def go(path: String) = Action.async { implicit request =>
    val data: Seq[FinalData]  = com.articlio.dataExecution.concrete.Importer.bulkImportRaw(path)
    val simplifiedAggregateDataStatus: Future[Boolean] = Future.sequence(data.map(_.accessOrError)) map { _.forall(_.isInstanceOf[Access]) }
    simplifiedAggregateDataStatus map { _ match {
      case true =>  Ok("Import successful")
      case false => Ok("Import failed")
    }}
  }
}

object showExtract extends Controller {
  def go(articleName: String,
                  pdb: String,
                  dataID: Option[Long] = None) = Action.async { implicit request =>

    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    import com.articlio.dataExecution._
    import com.articlio.dataExecution.concrete._
    import com.articlio.Globals.db
    
    FinalDataNew(SemanticData(articleName, pdb, dataID)()) flatMap { data =>
      data.accessOrError match {
        case error:  AccessError => Future.successful(Ok(s"couldn't find or create data for request: ${data.humanAccessMessage}")) 
        case access: Access => {
          val dataID = data.dataID.get
          db.query(Matches.filter(_.dataid === dataID).filter(_.docname === s"$articleName.xml").filter(_.fullmatch)) flatMap { 
            contentResult => db.query(Data.filter(_.datatype === data.dataType).filter(_.datatopic === articleName).map(_.dataid)) map {
              allApplicableDataIDs => Ok(views.html.showExtract(allApplicableDataIDs, dataID, pdb, articleName, contentResult.toList)) 
            }
          }
        }
      }
    } 
  }
}

object showOriginal extends Controller {
  def go(article: String) = Action { implicit request =>
    if (!article.startsWith("elife")) {
      val original = s"${config.config.getString("locations.pdf-source-input")}/$article.pdf".rooted
      println(s"serving $original")
      Ok.sendFile(content = new java.io.File(original),
                  inline = true) // as per https://www.playframework.com/documentation/2.1.3/ScalaStream
    }
    else
      Redirect(s"http://ubuntu.local:8000/${article}.xml") // assuming path ../data/eLife-JATS/2-styled is being web served
                                                           // e.g. by "python -m SimpleHTTPServer"
  }
}   
  
object adminPage extends Controller {
  def go = Action { implicit request => Ok(views.html.adminPage()) }
}  

object index extends Controller {
  def go = Action { implicit request => Ok(s"app is up, got request [$request]") }
}
  
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

