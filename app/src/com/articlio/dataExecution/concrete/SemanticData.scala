package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.ldb.ldbEngine
import com.articlio.util.runID
import com.articlio.dataExecution._
import models.Tables
import com.articlio.storage.{Connection}
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import models.Tables.{Data => DataRecord}
import scala.concurrent.Future
import com.articlio.Globals.db

case class SemanticData(articleName: String, 
                        ldbFile: String = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv",
                        ReQDataID: Option[Long] = None) // followed by a second parameter list that initializes its defaults from the former
                           (JATS: JATSData = JATSDataDisjunctiveSourced(articleName),
                            LDB: LDBData = LDBData(ldbFile)) 
                               extends DataObject(ReQDataID) {

  val dataTopic = articleName
  
  val dependsOn = Seq(JATS,LDB)
  
  def creator(runID: Long, dataType: String, fileName: String) : Future[Option[CreateError]] = {
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    ldbEngine(ldbFile).process(JATS.access)(runID, dataType, fileName) 
  }
}

