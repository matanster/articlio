package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.makeBrowserReady
import com.articlio.ldb.ldbEngine
import com.articlio.util.runID
import com.articlio.dataExecution._
import models.Tables
import com.articlio.storage.{Connection}
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import models.Tables.{Data => DataRecord}

case class SemanticAccess() extends Access

class SemanticData(articleName: String,ldbFile: String)
                  (JATS: JATSData = JATSData(articleName),
                   LDB: LDBData = LDBData(ldbFile)) 
                  extends Data with Connection {

  val dataType = "semantic"
  val dataTopic = articleName
  
  val dependsOn = Seq(JATS,LDB)
  
  val creator = ldbEngine(ldbFile).process(JATS.access)_ // underscore makes it a curried function
  
  val access = SemanticAccess()                          // no refined access details for now
}

