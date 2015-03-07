package controllers

import models.Tables._
import play.api._
import play.api.mvc._
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import play.api.http.MimeTypes
import com.articlio.dataExecution._
import com.articlio.dataExecution.concrete._
    
object Ldb extends Controller {

  import com.articlio.ldb
  import com.articlio.util.runID
  import com.articlio.input.JATS
  import com.articlio.config
  
  val pdb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"
  
  def getSemanticForArticle(articleName: String, pdb: String, dataID: Option[Long] = None) = {
    AttemptedDataObject(new SemanticData(articleName, pdb)())
  }
  
  def semanticFromArticle(articleName: String, 
                          pdb: String = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv") = 
                            Action { implicit request => Ok(getSemanticForArticle(articleName, pdb).humanAccessMessage) }

  def singleeLifeSourced(articleName: String,
                         pdb: String = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv") = Action { implicit request =>
    // TODO: fix or merge
    //pdb.go("SingleFileRun" + "-" + (new runID).id, new JATS(s"${config.eLife}/articleName"))
    //Ok("Done processing file")
    Ok("Not re-implemented")
  }

  import com.articlio.semantic.BulkFromDirectory
  def all = Action { implicit request =>
    val bulk = new BulkFromDirectory((new runID).id)
    bulk.all
    Ok("Done processing all files... but you probably timed out by now")
  }

  def allPdf = Action { implicit request =>
    val bulk = new BulkFromDirectory((new runID).id)
    bulk.allPDF
    Ok("Done processing all files... but you probably timed out by now")
  }

  def alleLife = Action { implicit request =>
    val bulk = new BulkFromDirectory((new runID).id)
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