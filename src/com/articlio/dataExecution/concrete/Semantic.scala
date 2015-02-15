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

case class Semantic(articleName: String, pdbFile: String) extends Data with Connection with Tables
{
  val dependsOn = Seq(JATS(articleName), 
                      PDB(pdbFile))
  
  def create : ReadyState = {
    val pdb = ldb(pdbFile)
    resultWrapper(pdb.goWrapper(articleName, dependsOn.head.access.path))
  }
  
  def ReadyState(suppliedRunID: Long): ReadyState = {
    DataRecord.filter(_.dataid === suppliedRunID).filter(_.datatype === this.getClass.getName).filter(_.datatopic === s"${articleName}.xml").list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
  
  def ReadyState(): ReadyState = {
    DataRecord.filter(_.datatype === this.getClass.getName).filter(_.datatopic === s"${articleName}.xml").list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
  
  val access = SemanticAccess() // no refined access details for now
}
