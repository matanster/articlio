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

import com.articlio.test.{TestSpec, TestContainer, Testable, Skip}
import com.articlio.test.FutureAdditions._
import scala.util.{Success, Failure}

object ShowExtract extends Controller with Testable {

  object TestContainer extends TestContainer {
    
    def tests = Seq(
        new TestSpec(given  = "a file non-existent in the data directory", 
                     should = "generate an error when semantic data is requested for it", 
                     TryNonExistentArticle),
        new TestSpec(given  = "a file existing in the data directory",
                     should = "have content results",
                     tryExistingArticle)
    )
        
    val existingArticle = "Eggers and Kaplan in Annals 2013 cognition and capabilities"
    val nonExistingArticle = "bla"
                                 
    def tryExistingArticle = {
      val pdb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"
      val dataID = None
      generatedResultsHaveContent_?(existingArticle, pdb, dataID)
    }
    
    def TryNonExistentArticle = {
      val pdb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"
      val dataID = None
      generatesResults_?(nonExistingArticle, pdb, dataID).reverse
    }
    
    def generatesResults_?(articleName: String, pdb: String, dataID: Option[Long] = None) = {
      api(articleName, pdb, dataID)
    }
    
    def generatedResultsHaveContent_?(articleName: String, pdb: String, dataID: Option[Long] = None) = {
      api(articleName, pdb, dataID) map {
      case (dataID, allApplicableDataIDs, contentResult) =>
        println(contentResult.toList.length)
        if (contentResult.toList.length == 0) {
          println("error")
          throw new Throwable("no results generated") 
        }
      }
    }
  }
  
  def api(articleName: String, pdb: String, dataID: Option[Long] = None) = {
    
    import com.articlio.dataExecution._
    import com.articlio.dataExecution.concrete._
    import com.articlio.Globals.db
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    
    FinalDataNew(SemanticData(articleName, pdb, dataID)()) flatMap { data =>
      println("in processing created FinalDataNew")
      data.accessOrError match {
        case error:  AccessError => {
          Future.failed(new Throwable(s"couldn't find or create data for request: ${data.humanAccessMessage}"))
        }
        case access: Access => {
          val dataID = data.dataID.get
          getData(dataID, data.dataType, articleName) map { case (allApplicableDataIDs, contentResult) =>
            println(allApplicableDataIDs)
            (dataID, allApplicableDataIDs, contentResult) }  
        }
      }
    }
  }
  
  def UI(articleName: String, pdb: String, dataID: Option[Long] = None) = Action.async { implicit request =>
    
    api(articleName, pdb, dataID) map { case (dataID, allApplicableDataIDs, contentResult) =>
      println(s"result length: ${contentResult.toList.length}")
      Ok(views.html.showExtract(allApplicableDataIDs, dataID, pdb, articleName, contentResult.toList))
    } 
  }
  
  private def allApplicableDataIDs(dataType: String, dataTopic: String) = 
    db.query(Data.filter(_.datatype === dataType).filter(_.datatopic === dataTopic).map(_.dataid))
    
  private def getMatches(dataID: Long, articleName: String) = 
    db.query(Matches.filter(_.dataid === dataID).filter(_.docname === s"$articleName.xml").filter(_.fullmatch))
  
  private def getData(dataID: Long, dataType: String, articleName: String) = 
    getMatches(dataID, articleName) flatMap { 
      contentResult => allApplicableDataIDs(dataType, articleName) map {
        allApplicableDataIDs => (allApplicableDataIDs, contentResult.toList) 
      }
    }
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

