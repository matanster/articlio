package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import play.api.http.MimeTypes

object Ldb extends Controller with Tables {

  import com.articlio.ldb
  import com.articlio.util.runID
  import com.articlio.input.JATS
  import com.articlio.config
  
  def singlePdfSourced(inputFileName: String) = Action { implicit request =>
    ldb.ldb.go("SingleFileRun" + "-" + (new runID).id, new JATS(s"${config.pdf}/$inputFileName", "pdf-converted"))
    Ok("Done processing file")
  }

  def singleeLifeSourced(inputFileName: String) = Action { implicit request =>
    ldb.ldb.go("SingleFileRun" + "-" + (new runID).id, new JATS(s"${config.eLife}/$inputFileName"))
    Ok("Done processing file")
  }

  import com.articlio.semantic.Bulk
  def all = Action { implicit request =>
    val bulk = new Bulk((new runID).id)
    bulk.all
    Ok("Done processing all files... but you probably timed out by now")
  }

  def allPdf = Action { implicit request =>
    val bulk = new Bulk((new runID).id)
    bulk.allPDF
    Ok("Done processing all files... but you probably timed out by now")
  }

  def alleLife = Action { implicit request =>
    val bulk = new Bulk((new runID).id)
    bulk.alleLife
    Ok("Done processing all files... but you probably timed out by now")
  }

  def export = Action { implicit request =>
    com.articlio.storage.createCSV.go()
    Ok("Done producing result CSVs")
  }

  def exportAnalytic = Action { implicit request =>
    com.articlio.storage.createAnalyticSummary.go()
    Ok("Done producing analytic result CSVs")
  }

  import com.articlio.semantic.AppActorSystem
  def purge = Action { implicit request =>
    AppActorSystem.outDB ! "dropCreate"
    Ok("purging some data... see the source code for details")
  }
}