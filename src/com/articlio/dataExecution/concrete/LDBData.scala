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
  
  // for now, no real creation for an ldb, this is just a stub.
  def create()(runID: Long, ldb: String) : Option[CreateError] = Some(CreateError("for now, linguistic database must be present on disk, cannot be created from scratch by the software.")) 
  val creator = create()_
  
  // for now, availability of an ldb is not managed through the data status database. so just check if it's there or not, for now.
  override def ReadyState: ReadyState = {
    filePathExists(s"${config.ldb}/$csvFileName") match {
      case true => Ready(0L) // for now. see comment above.
      case false => NotReady
    }
  } 

  val access = LDBaccess(config.JATSout)
}


  
    
  