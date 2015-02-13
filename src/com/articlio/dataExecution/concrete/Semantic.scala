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

case class SemanticAccess() extends Access

case class Semantic(articleName: String, runID: BigInt) extends Data with Connection with Tables
{
  val dependsOn = Seq(JATS(articleName))
  
  def create : ReadyState = {
    resultWrapper(ldb.goWrapper(articleName, dependsOn.head.access.path))
  }
  
  def ReadyState: ReadyState = {
    println(s"result matches count for $runID: ${Matches.filter(_.runid === runID).filter(_.docname === s"${articleName}.xml").list.size}")
    Matches.filter(_.runid === runID).filter(_.docname === s"${articleName}.xml").list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
  
  val access = SemanticAccess() // no refined access details for now
}
