package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.JATScreateSingle
import com.articlio.ldb.ldb
import com.articlio.util.runID
import com.articlio.dataExecution._
import models.Tables
import com.articlio.storage.{Connection}
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import models.Tables.{Data => DataRecord}

case class SemanticAccess() extends Access

case class SemanticData(articleName: String, ldbFile: String) extends Data with Connection
{
  val dataType = "semantic"
  val dataTopic = articleName

  val JATS = JATSData(articleName)
  val LDB  = LDBData(ldbFile)
  val dependsOn = Seq(JATS,LDB)
  
  val creator = ldb(ldbFile).go(JATS.access)_ // currying to let other caller fill in the other parameters
  
  val access = SemanticAccess()               // no refined access details for now
}
