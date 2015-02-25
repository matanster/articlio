package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.ReadyJATS

case class LDBaccess(dirPath: String) extends Access

case class LDBData(csvFileName: String) extends DataObject
{
  val dataType = "LDB"
  
  val dataTopic = csvFileName // for now
  
  val dependsOn = Seq()
  
  val fullPath = s"${config.ldb}/$csvFileName"
  
  // this is just a stub. no real creation for a source pdf file (for now, maybe later, try to fetch it from distributed storage?)
  def create()(runID: Long, dataType: String, fileName: String) : Option[CreateError] = {
    filePathExists(fullPath) match {
      case true  => None 
      case false => Some(CreateError("ldb file $fileName was not found, so it could not be imported.")) 
    }
  }; val creator = create()_ // curried, alternatively could be a partial application (if creator collapses to single param list: create(_ :Long, _ :String))
  
  
  val access = LDBaccess(config.JATSout)
}