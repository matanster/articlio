package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._

import util._
import com.articlio.config
import com.articlio.pipe.pipelines.JATScreateSingle

case class JATSaccess(path: String) extends Access

case class JATS(articleName: String) extends DataWrapper
{
  val dependsOn = Seq()
  
  def isReady: Boolean = {
    filePathExists(s"${config.eLife}/$articleName")
  } 
  
  def create : Boolean = {
    try {
      new JATScreateSingle(articleName) // TODO: need be a wrapper of it
      return true 
      } catch { case _ : Throwable => return false}
  }  

  val access = JATSaccess(config.JATSout)
}
