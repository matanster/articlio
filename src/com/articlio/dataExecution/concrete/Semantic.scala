package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.JATScreateSingle
import com.articlio.ldb.ldb
import com.articlio.util.runID
import com.articlio.dataExecution._

import com.articlio.storage.{Connection, Match}
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._

case class SemanticAccess() extends Access

case class Semantic(articleName: String, runID: String) extends DataWrapper with Connection with Match
{
  val dependsOn = Seq(JATS(articleName))
  
  def create : ReadyState = {
    wrapper(ldb.goWrapper(articleName, dependsOn.head.access.path))
  }
  
  def ReadyState: ReadyState = {
    println(s"result matches count for $runID: ${matches.filter(_.runID === runID).filter(_.docName === s"${articleName}.xml").list.size}")
    matches.filter(_.runID === runID).filter(_.docName === s"${articleName}.xml").list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
  
  val access = SemanticAccess() // no refined access details for now
}
