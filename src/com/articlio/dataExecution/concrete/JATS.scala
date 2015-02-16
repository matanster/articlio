package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.pipe.pipelines.JATScreateSingle

case class JATSaccess(path: String) extends Access

case class JATS(articleName: String) extends Data
{
  val dataType = "JATS"
  val dataTopic = articleName
  
  val dependsOn = Seq()
  
  val creator = (new JATScreateSingle).go(_ :Long, _ :String)

  val access = JATSaccess(config.JATSout) // filePathExists(s"${config.eLife}/$articleName.xml") match {
}
