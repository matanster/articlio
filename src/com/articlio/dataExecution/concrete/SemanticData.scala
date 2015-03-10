package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.ReadyJATS
import com.articlio.ldb.ldbEngine
import com.articlio.util.runID
import com.articlio.dataExecution._
import models.Tables
import com.articlio.storage.{Connection}
import play.api.db.slick._
import slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import models.Tables.{Data => DataRecord}

case class SemanticAccess() extends Access

case class SemanticData(articleName: String, 
                        ldbFile: String = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv",
                        ReQDataID: Option[Long] = None)
                       (JATS: JATSData = JATSDataDisjunctiveSourced(articleName),
                        LDB: LDBData = LDBData(ldbFile)) extends DataObject(ReQDataID) {

  //val dataType = "semantic"
  
  val dataTopic = articleName
  
  val dependsOn = Seq(JATS,LDB)
  
  val creator = ldbEngine(ldbFile).process(JATS.access)_ // underscore makes it a curried function
  
  val access = SemanticAccess()                          // no refined access details for now
}

