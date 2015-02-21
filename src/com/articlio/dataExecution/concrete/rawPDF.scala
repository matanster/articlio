package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config

case class rawPDFaccess(dirPath: String) extends Access

case class rawPDFData(fileName: String) extends Data
{
  val dataType = "RawPDF"
  
  val dataTopic = fileName // for now
  
  val dependsOn = Seq()
  
  // this is just a stub. no real creation for a source pdf file (for now, maybe later, try to fetch it from distributed storage?)
  def createLdb(runID: Long, fileName: String) : Option[CreateError] = Some(CreateError("requested pdf file $fileName was not found.")) 
  val creator = createLdb(_ :Long, _ :String)
  
  val fullPath = s"${config.pdfSourceDir}/fileName"
  
  // for now, its availability is not managed through the data status database. so just check if it's there or not, for now.
  override def ReadyState: ReadyState = {
    filePathExists(fullPath) match {
      case true => Ready(0L) // for now. see comment above.
      case false => NotReady
    }
  } 

  val access = rawPDFaccess(fullPath)
}


  
    
  