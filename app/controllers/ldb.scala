package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import play.api.http.MimeTypes
import com.articlio.dataExecution._
import com.articlio.dataExecution.concrete._
    
object Ldb extends Controller with Tables {

  import com.articlio.ldb
  import com.articlio.util.runID
  import com.articlio.input.JATS
  import com.articlio.config
  
  val pdb = ldb.ldb("Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv")
  
  def singlePdfSourced(articleName: String, pdb: 
                       String = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv", 
                       runID: Option[Long]) = Action { implicit request =>
    val executionManager = new DataExecutionManager
    executionManager.getDataAccess(Semantic(articleName, pdb, runID.get)) match {
      case None =>
        Ok("Result data failed to create. Please contact development with all necessary details (url, and description of what you were doing)")
      case dataAccessDetail : Some[Access] =>  
        Ok("Done processing file")
    }
  }

  def singleeLifeSourced(articleName: String) = Action { implicit request =>
    pdb.go("SingleFileRun" + "-" + (new runID).id, new JATS(s"${config.eLife}/articleName"))
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