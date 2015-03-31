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

import com.articlio.test.{TestSpec, UnitTestable, Testable}
import com.articlio.test.FutureAdditions._
import scala.util.{Success, Failure}

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
