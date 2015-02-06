package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.JATScreateSingle
import com.articlio.ldb.ldb
import com.articlio.util.runID

case class SemanticAccess() extends Access

case class Semantic(articleName: String) extends DataWrapper
{
  val dependsOn = Seq(JATS(articleName))
  
  def create : Boolean = {
    try {
      ldb.goWrapper(articleName, dependsOn.head.access.path)
      return true 
      } catch { case _ : Throwable => return false}
  }  

  def isReady: Boolean = {
      true // for now
  } 
  
  val access = SemanticAccess() // TODO
}
