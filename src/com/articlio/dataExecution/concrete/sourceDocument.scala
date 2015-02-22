package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config

@deprecated("redundant", "redundant")
case class sourceDocument(fileName: String) extends Data
{
  import scala.slick.driver.MySQLDriver.simple._
  import scala.slick.jdbc.meta._
  import models.Tables._
  import models.Tables.{Data => DataRecord}
  import com.articlio.storage.Connection

  val dataType = "sourceDocument"
  
  val dataTopic = fileName // for now
  
  val rawPDF       = RawPDF(fileName)
  val raweLifeJATS = RaweLifeJATS(fileName)
  val dependsOn = Seq()
  
  val fullPath = s"${config.config.getString("locations.pdf-input")}/$fileName"
  
  override def ReadyStateAny(): ReadyState = {
    val rawPDFReadyState = rawPDF.ReadyStateAny
    val raweLifeJATSReadyState = raweLifeJATS.ReadyStateAny
    NotReady
  } 

  // this is just a stub. no real creation for a source pdf file (for now, maybe later, try to fetch it from distributed storage?)
  def create()(dataID: Long, articleName:String) : Option[CreateError] = { 
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError("source pdf file $fileName was not found.")) 
    }
  }; val creator = create()_ 

  val access = RawPDFaccess(fullPath)
}


case class RawPDFaccess(dirPath: String) extends Access
case class RawPDF(fileName: String) extends Data
{
  val dataType = "RawPDF"
  
  val dataTopic = fileName // for now
  
  val dependsOn = Seq()
  
  val fullPath = s"${config.config.getString("locations.pdf-input")}/$fileName"
  
  // this is just a stub. no real creation for a source pdf file (for now, maybe later, try to fetch it from distributed storage?)
  def create()(dataID: Long, fileName: String) : Option[CreateError] = {
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError("source pdf file $fileName was not found.")) 
    }
  }; val creator = create()_

  val access = RawPDFaccess(fullPath)
}

case class RaweLifeJATSAccess(dirPath: String) extends Access
case class RaweLifeJATS(fileName: String) extends Data
{
  val dataType = "RaweLifeJATS"
  
  val dataTopic = fileName // for now
  
  val dependsOn = Seq()
  
  val fullPath = s"${config.config.getString("locations.JATS-input.input")}/$fileName"
  
  // this is just a stub. no real creation for a source pdf file (for now, maybe later, try to fetch it from distributed storage?)
  def create()(runID: Long, fileName: String) : Option[CreateError] = {
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError("source eLife JATS file $fileName was not found.")) 
    }
  }; val creator = create()_ // curried, alternatively could be a partial application (if creator collapses to single param list: create(_ :Long, _ :String))

  val access = RaweLifeJATSAccess(fullPath)
}