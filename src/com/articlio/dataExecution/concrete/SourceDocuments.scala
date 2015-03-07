package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config

/* 
 *  These data classes effectively just "import" a file from a file system type -
 *  they register it in the Data DB, and thus make it available for processing. 
 */
 
abstract class Raw extends DataObject

case class RawPDFaccess(dirPath: String) extends Access
case class RawPDF(articleName: String) extends Raw // TODO: connect with distributed storage local caching
{
  //val dataType = "RawPDF"
  
  val dataTopic = articleName // for now
  
  val dependsOn = Seq()
  
  val fileName = s"$articleName.pdf"
  
  val fullPath = s"${config.config.getString("locations.pdf-source-input")}/$fileName"

  // TODO: hook into distributed storage local caching
  def importer()(dataID: Long, dataType:String, fileName: String) : Option[CreateError] = {
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError(s"source pdf file $fileName was not found."))
    }
  }; val creator = importer()_

  val access = RawPDFaccess(fullPath)
}

case class RaweLifeJATSAccess(dirPath: String) extends Access
case class RaweLifeJATS(articleName: String) extends Raw // TODO: connect with distributed storage local caching
{
  //val dataType = "RaweLifeJATS"
  
  val dataTopic = articleName // for now
  
  val dependsOn = Seq()
  
  val fileName = s"$articleName.xml"
  
  val fullPath = s"${config.config.getString("locations.JATS-input.input")}/$fileName"
  
  def importer()(runID: Long, dataType: String, fileName: String) : Option[CreateError] = {
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError(s"source eLife JATS file $fileName was not found.")) 
    }
  }; val creator = importer()_ // curried, alternatively could be a partial application (if creator collapses to single param list: create(_ :Long, _ :String))

  val access = RaweLifeJATSAccess(fullPath)
}

object Importer {

  // guessfully type raw input
  def rawGuessImport(path: String): Option[Raw] = {
    val fileName = path.split("/").last // not for Windows...
    fileName match {
      case s: String if s.endsWith(".pdf") => Some(RawPDF(path))
      case s: String if s.endsWith(".xml") => Some(RaweLifeJATS(path))
      case _                               => println(s"Could not guess raw file type for file $path"); None 
    }
  }
  
  def bulkImport(path: String) : Boolean = { // TODO: implement a variant of this, that avoids md5 hash-wise duplicate files
                                 //       to avoid bloated data groups, and reduce statistic skew from duplicates
    val files = new java.io.File(/* path */"/home/matan/Downloads/articles").listFiles.filter(file => (file.isFile)).map(_.getName) 
    files.map(rawGuessImport).flatten.map(AttemptDataObject) // nothing to do with the return value here 
    true
  }
}






@deprecated("redundant", "redundant")
case class sourceDocument(fileName: String) extends DataObject
{
  import scala.slick.driver.MySQLDriver.simple._
  import scala.slick.jdbc.meta._
  import models.Tables._
  import models.Tables.{Data => DataRecord}
  import com.articlio.storage.Connection

  //val dataType = "sourceDocument"
  
  val dataTopic = fileName // for now
  
  val rawPDF       = RawPDF(fileName)
  val raweLifeJATS = RaweLifeJATS(fileName)
  val dependsOn = Seq()
  
  val fullPath = s"${config.config.getString("locations.pdf-source-input")}/$fileName"
  
  override def ReadyStateAny(): ReadyState = {
    val rawPDFReadyState = rawPDF.ReadyStateAny
    val raweLifeJATSReadyState = raweLifeJATS.ReadyStateAny
    new NotReady
  } 

  // this is just a stub. no real creation for a source pdf file (for now, maybe later, try to fetch it from distributed storage?)
  def create()(dataID: Long, dataType: String, articleName:String) : Option[CreateError] = {
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError(s"source pdf file $fileName was not found.")) 
    }
  }; val creator = create()_ 

  val access = RawPDFaccess(fullPath)
}
