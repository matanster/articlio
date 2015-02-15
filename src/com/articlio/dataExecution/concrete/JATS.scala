package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.JATScreateSingle

case class JATSaccess(path: String) extends Access

case class JATS(articleName: String) extends Data
{
  val dependsOn = Seq()
  
  def ReadyState: ReadyState = {
    filePathExists(s"${config.eLife}/$articleName.xml") match {
      case true => Ready
      case false => NotReady
    }
  }
  
  def ReadyState(suppliedRunID: Long) = ReadyState
  
  def create : ReadyState = {
    resultWrapper(new JATScreateSingle(s"$articleName").go)
  }  

  val access = JATSaccess(config.JATSout)
}
