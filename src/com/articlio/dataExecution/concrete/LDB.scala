package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.JATScreateSingle

case class PDBaccess(path: String) extends Access

case class PDB(csvFileName: String) extends Data
{
  val dependsOn = Seq()
  
  def ReadyState: ReadyState = {
    filePathExists(s"${config.ldb}/$csvFileName") match {
      case true => Ready
      case false => NotReady
    }
  } 

  def ReadyState(suppliedRunID: Long) = ReadyState
  
  def create : ReadyState = Ready // there's no real creation for an ldb (for now)

  val access = JATSaccess(config.JATSout)
}
