package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config

@deprecated("redundant", "redundant")
case class sourceDocument(fileName: String) extends DataObject
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
  def create()(dataID: Long, dataType: String, articleName:String) : Option[CreateError] = { 
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError("source pdf file $fileName was not found.")) 
    }
  }; val creator = create()_ 

  val access = RawPDFaccess(fullPath)
}

/* 
 *  These data classes effectively just "import" a file from a file system type -
 *  they register it in the Data DB, and thus make it available for processing. 
 */
 
abstract class Raw extends DataObject

case class RawPDFaccess(dirPath: String) extends Access
case class RawPDF(fileName: String) extends Raw
{
  val dataType = "RawPDF"
  
  val dataTopic = fileName // for now
  
  val dependsOn = Seq()
  
  val fullPath = s"${config.config.getString("locations.pdf-input")}/$fileName"
  
  // this is just a stub. no real creation for a source pdf file (for now, maybe later, try to fetch it from distributed storage?)
  def create()(dataID: Long, dataType:String, fileName: String) : Option[CreateError] = {
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError("source pdf file $fileName was not found.")) 
    }
  }; val creator = create()_

  val access = RawPDFaccess(fullPath)
}

case class RaweLifeJATSAccess(dirPath: String) extends Access
case class RaweLifeJATS(fileName: String) extends Raw
{
  val dataType = "RaweLifeJATS"
  
  val dataTopic = fileName // for now
  
  val dependsOn = Seq()
  
  val fullPath = s"${config.config.getString("locations.JATS-input.input")}/$fileName"
  
  // this is just a stub. no real creation for a source pdf file (for now, maybe later, try to fetch it from distributed storage?)
  def create()(runID: Long, dataType: String, fileName: String) : Option[CreateError] = {
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError("source eLife JATS file $fileName was not found.")) 
    }
  }; val creator = create()_ // curried, alternatively could be a partial application (if creator collapses to single param list: create(_ :Long, _ :String))

  val access = RaweLifeJATSAccess(fullPath)
}

object Importers {

  // guessfully type raw input
  def rawGuessImport(path: String): Option[Raw] = {
    val fileName = path.split("/").last // not for Windows...
    fileName match {
      case s: String if s.endsWith(".pdf") => Some(RawPDF(path))
      case s: String if s.endsWith(".xml") => Some(RaweLifeJATS(path))
      case _                               => println(s"Could not guess raw file type for file $path"); None 
    }
  }
  
  def bulkImport(path: String) { // TODO: implement a variant of this, that avoids md5 hash-wise duplicate files
                                 //       to avoid bloated data groups, and reduce statistic skew from duplicates
    val files = new java.io.File(path).listFiles.filter(file => (file.isFile)).map(_.getName) 
    val executionManager = new DataExecutionManager
    files.map(rawGuessImport).flatten.map(executionManager.getSingleDataAccess) // nothing to do with the return value here 
  }
}