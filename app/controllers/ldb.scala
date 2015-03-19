package controllers

import models.Tables._
import play.api._
import play.api.mvc._
//import play.api.db.slick._ play slick plugin is not yet interoperable with Slick 3.0.0
//import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import play.api.http.MimeTypes
import com.articlio.dataExecution._
import com.articlio.dataExecution.concrete._
    
object SemanticExtractor extends Controller {

  import com.articlio.ldb
  import com.articlio.util.runID
  //import com.articlio.input.JATS
  import com.articlio.config
  
  val pdb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"
  
  def fromArticle(articleName: String, pdb: String) = Action.async { 
    implicit request =>
      implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
      FinalData(SemanticData(articleName, pdb)()).humanAccessMessage map { message => Ok(message) } 
  }
  
  def fromTextFile(articleName: String, pdb: String) = Action.async {
    implicit request => 
      implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
      FinalData(SemanticData(articleName, pdb)(JATS = JATSDataFromTxtFile(articleName)())).humanAccessMessage map { message => Ok(message) } 
  }

  def fromElife(articleName: String,
                         pdb: String = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv") = Action { implicit request =>
    // TODO: fix or merge
    //pdb.go("SingleFileRun" + "-" + (new runID).id, new JATS(s"${config.eLife}/articleName"))
    //Ok("Done processing file")
    Ok("Not (re-)implemented")
  }

  import com.articlio.BulkFromDirectory
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
    com.articlio.storage.createCSV.go(0L) // TODO: need to get parameter, can't be parameter-less 
    Ok("Done producing result CSVs")
  }

  def exportAnalytic = Action { implicit request =>
    com.articlio.storage.createAnalyticSummary.go
    Ok("Done producing analytic result CSVs")
  }

  import com.articlio.AppActorSystem
  def purge = Action { implicit request =>
    AppActorSystem.outDB ! "dropCreate"
    Ok("purging all data...") // fire and forget
  }
}