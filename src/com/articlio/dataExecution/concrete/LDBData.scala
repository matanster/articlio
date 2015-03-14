package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.ReadyJATS
import scala.concurrent.Future

case class LDBaccess(dirPath: String) extends Access

case class LDBData(csvFileName: String) extends DataObject
{
  //val dataType = "LDB"
  
  val dataTopic = csvFileName // for now
  
  val dependsOn = Seq()
  
  val fullPath = s"${config.ldb}/$csvFileName"
  
  // just tests that the ldb file is there (maybe later, try to fetch it from distributed storage instead)
  def creator(runID: Long, dataType: String, fileName: String) : Future[Option[CreateError]] = {
    Future.successful {
      filePathExists(fullPath) match {
        case true  => None 
        case false => Some(CreateError("ldb file $fileName was not found, so it could not be imported.")) 
      }
    }
  }; // val creator = importer()_ // curried, alternatively could be a partial application (if creator collapses to single param list: create(_ :Long, _ :String))
  
  val access = LDBaccess(config.JATSout)
}