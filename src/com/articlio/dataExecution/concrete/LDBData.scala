package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import scala.concurrent.Future
import com.articlio.Globals.db
import com.articlio.storage.ManagedDataFiles._

case class LDBaccess(dirPath: String) extends Access

case class LDBData(csvFileName: String) extends DataObject
{
  val dataTopic = csvFileName // for now
  
  val dependsOn = Seq()
  
  val fullPath = s"${config.ldb}/$csvFileName".rooted
  
  // just tests that the ldb file is there (maybe later, try to fetch it from distributed storage instead)
  def creator(runID: Long, dataType: String, fileName: String) : Future[Option[CreateError]] = {
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    Future {
      filePathExists(fullPath) match {
        case true  => None 
        case false => Some(CreateError("ldb file $fileName was not found, so it could not be imported.")) 
      }
    }
  }
  
  val access = LDBaccess(config.JATSout.rooted)
}