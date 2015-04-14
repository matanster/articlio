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
import com.articlio.dataExecution.Access
import com.articlio.config
import com.articlio.storage.ManagedDataFiles._
import scala.util.{Success, Failure, Try}
import com.articlio.test.{TestSpec, TestContainer, Testable, Skip, Only}
import com.articlio.util.FutureAdditions._
import scala.util.{Success, Failure}
import com.articlio.dataExecution.concrete._
import com.articlio.dataExecution._

object BulkFromRaw extends Controller with Testable {
  
  object TestContainer extends TestContainer {
    
    def tests = Seq(new TestSpec(given = "an existing groupID of Raw data type",
                                 should = "generate semantic data for them all",
                                 teste)
                )
    
    def teste: Future[Unit] = {
      controllers.BulkImportRaw.TestContainer.succeedWithTestResources map { groupID => 
        println(Console.GREEN_B + "returned groupID: " + groupID.get + Console.RESET)
        val a = api(groupID.get)
        //throw new Throwable("booooo")
        a.onComplete { 
          case Failure(t) => println(t)
          case Success(s) => println(s)
        }
        a.map { seq => println(Console.RED_B + "in map of api return"); if (!seq.forall(_.error == None)) 
          throw new Throwable(s"bulk failed for the following item/s: ${seq.filter(_.error != None)}") }
      }     
    }
  }

  def UI(groupID: Long) = Action.async { implicit request =>
    api(groupID) map { seq => seq.forall(_.error == None) match {
      case true =>  Ok("Bulk operation successful")
      case false => Ok("operation failed")
    }}
  }
  
  def api(groupID: Long): Future[Seq[FinalData]] = {
    val ldbData = LDBData("Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv")
    com.articlio.dataExecution.BulkImpl.SemanticForGroup(groupID, ldbData)
  }
}
