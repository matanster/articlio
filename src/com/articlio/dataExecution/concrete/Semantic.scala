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

case class Semantic(articleName: String, pdbFile: String) extends DBdata(articleName) with Connection
{
  val dependsOn = Seq(JATS(articleName), 
                      PDB(pdbFile))
  
  val creator = ldb(pdbFile).go(_:Long, articleName, new com.articlio.input.JATS(s"${dependsOn.head.access.path}/$articleName.xml"))
  
  val dataType = "semantic"
  
  val dataTopic = articleName
  
  val access = SemanticAccess() // no refined access details for now
}
