package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.makeBrowserReady

case class JATSaccess(dirPath: String) extends Access
case class JATSData(articleName: String) extends Data
{
  val dataType = "JATS"
  
  val dataTopic = articleName
  
  val dependsOn = Seq(SourceJATS(articleName))
  
  val creator = (new makeBrowserReady).go(_ :Long, _ :String)

  val access = JATSaccess(config.config.getString("locations.JATS")) // filePathExists(s"${config.eLife}/$articleName.xml") match {
}

//
// get either pdf sourced, or JATS sourced, input, whichever exists for the requested article name.
// this aspect would be refactored, when 
//
case class SourceJATSaccess(dirPath: String) extends Access
case class SourceJATS(articleName: String) extends Data
{
  val dataType = "SourceJATS"
  
  val dataTopic = articleName
  
  val dependsOn = Seq()
  
  def creator(dataID: Long, articleName:String) : Option[CreateError] = {
             
  }

  val access = SourceJATSaccess("TBD") // filePathExists(s"${config.eLife}/$articleName.xml") match {
}
