package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import scala.concurrent.Future
import com.articlio.Globals.db
import com.articlio.storage.ManagedDataFiles._

/* 
 *  These data classes effectively just "import" a file from a file system type -
 *  they register it in the Data DB, thus making it available for processing by higher-up data entities. 
 */
 
abstract class Raw(articleName: String, externalSourceDirectory: Option[String] = None) extends DataObject 
{
  // TODO: hook into distributed storage local caching
  
  val dataTopic = articleName // for now
  
  val dependsOn = Seq()
  
  val expectedFileExtension: String
  
  lazy val fileName = s"$articleName.$expectedFileExtension"
  
  val directory: String
  
  lazy val fullPath = s"$directory/$fileName".rooted

  // check if file is already sitting in the managed directory. if not, try to import it from the import-from directory argument, if supplied.  
  // a file with the exact same name as one already in the managed directory, will not be imported.
  def creator(dataID: Long, dataType:String, fileName: String) : Future[Option[CreateError]] = {
    filePathExists(fullPath) match {
      case true  => Future.successful(None)
      case false => 
        externalSourceDirectory match {
          case None => Future.successful(Some(CreateError(s"file $fileName was not found in $directory")))
          case Some(externalSourceDirectory) => filePathExists(s"$externalSourceDirectory/$fileName") match {
            case true => {
              com.articlio.pipelines.util.copy(s""""$externalSourceDirectory/$fileName"""", directory)
              Future.successful(None) }
            case false =>
              Future.successful(Some(CreateError(s"file $fileName was not found in $externalSourceDirectory")))
          }
        }
    }
  }
  val access = new Access
}

case class RawPDF(articleName: String, externalSourceDirectory: Option[String] = None) extends Raw(articleName, externalSourceDirectory) 
{
  override val expectedFileExtension = "pdf"
  override val directory = config.config.getString("locations.pdf-source-input").rooted
}

case class RaweLifeJATS(articleName: String, externalSourceDirectory: Option[String] = None) extends Raw(articleName, externalSourceDirectory) 
{
  val expectedFileExtension = "xml"
  val directory = config.config.getString("locations.JATS-input.input").rooted
}

case class RawTxtFile(articleName: String, externalSourceDirectory: Option[String] = None) extends Raw(articleName, externalSourceDirectory) 
{
  val expectedFileExtension = "txt"
  val directory = config.config.getString("locations.txtFile-source-input").rooted
}

object Importer { // not for Windows OS...

  def filePathEscape(path: String) = path.replace(" ", "\\ ")
  
  // guessfully type raw input
  def importCreateData(fileName: String, path: String): Option[Raw] = {
    println(s"importing file $fileName")
    //val fileName = path.split("/").last 
    fileName match {
      case s: String if s.endsWith(".pdf") => Some(RawPDF(fileName, Some(path)))
      case s: String if s.endsWith(".xml") => Some(RaweLifeJATS(fileName, Some(path)))
      case s: String if s.endsWith(".txt") => Some(RawTxtFile(fileName, Some(path)))
      case _ => println(s"Could not find matching handling for file $fileName - file not imported (is it the right file extension?)"); None 
    }
  }
  
  def bulkImportRaw(path: String): Seq[Future[FinalDataNew]] = { 
    // TODO: implement a variant of this, that avoids md5 hash-wise duplicate files
    //       to avoid bloated data groups, thus also reducing statistic skew from duplicates
    // TODO: do this more asynchronously if it becomes a key process (cf. http://docs.oracle.com/javase/7/docs/api/java/nio/file/DirectoryStream.html or other)
    import com.articlio.pipelines.util.copy
    import play.api.libs.concurrent.Execution.Implicits.defaultContext

    val liftedPath = new java.io.File(path) 
    liftedPath.exists match {
      case false => throw new Throwable(s"Cannot import from non-existent directory: $path")
      case true => {
        val files = liftedPath.listFiles.filter(file => (file.isFile)).map(file => file.getName).toSeq
        files.map(fileName => importCreateData(fileName, path)).flatten // flatten only takes Somes into the result list
                                                           .map(data => FinalDataNew(data))
      }
    }
  }
}
