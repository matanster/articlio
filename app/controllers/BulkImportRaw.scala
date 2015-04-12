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
import com.articlio.dataExecution._
import com.articlio.dataExecution.Access
import com.articlio.config
import com.articlio.storage.ManagedDataFiles._
import scala.util.{Success, Failure, Try}

import com.articlio.test._
import com.articlio.test.FutureAdditions._
import scala.util.{Success, Failure}

object BulkImportRaw extends Controller with Testable {
  
  object TestContainer extends TestContainer {
    
    def tests = Seq(new TestSpec(given = "the test-resources directory containing pdf files",
                                 should = "import them successfully into the system",
                                 succeedWithTestResources),
                    new TestSpec(given = "a non-existing directory",
                                 should = "fail trying to import from it",
                                 failWithNonExistentLocation)
                )
    
    def succeedWithTestResources: Future[Unit] = 
      api("test-resources") map { result => if (!result) throw new Throwable("import failed") }

    def failWithNonExistentLocation: Future[Unit] = 
      api("bla bla foo") map { _ match {
        case false => Unit 
        case true  => throw new Throwable("should have failed")
        }
      }
  }

  def UI(path: String) = Action.async { implicit request =>
    api(path) map { _ match {
      case true =>  Ok("Import successful")
      case false => Ok("Import failed")
    }}
  }
  
  def api(path: String): Future[Boolean] = {
    com.articlio.dataExecution.concrete.Importer.bulkImportRaw(path) map { seqOfData => 
      Try(seqOfData) match {
        case Success(seq) => seq.forall(_.error == None) 
        case Failure(t)   => false
      }
    }
  }
}
