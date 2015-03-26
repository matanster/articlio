package com.articlio
import com.typesafe.config.ConfigFactory
import java.io.File
import com.articlio.Globals.dataFilesRoot
import com.articlio.storage.ManagedDataFiles._

// TODO: leave this object just making the connection to the config file, 
//       letting callers pick their key. that will reduce bloat and make
//       callers more traceable. 

object config {
  
  val config = ConfigFactory.parseFile(new File("../config/config.json"))

  val ldb = config.getString("locations.ldb")
  val output = config.getString("locations.semantic-logging").rooted
  val eLife = config.getString("locations.JATS").rooted
  val pdf = config.getString("locations.JATS").rooted

  val pdfAsJATS = config.getString("locations.pdf-source-extraction.JATS").rooted
  val copyTo = config.getString("locations.JATS").rooted  
  val asText = config.getString("locations.pdf-source-extraction.Text").rooted
  val asEscapedText = config.getString("locations.pdf-source-extraction.EscapedText").rooted
  
  val JATSinput = config.getString("locations.JATS-input.input").rooted
  val JATSformatted = config.getString("locations.JATS-input.formatted").rooted
  val JATSstyled = config.getString("locations.JATS-input.styled").rooted
  val JATSout = config.getString("locations.JATS").rooted
  
  val nodejsServerUrl = s"http://${config.getString("http-services.pdf-sourceExtractor.host")}:${config.getString("http-services.pdf-sourceExtractor.port")}"
  
  val host       = config.getString("http-services.scala-main.host")
  val port       = config.getString("http-services.scala-main.port")
  val ownRootUrl = s"http://$host:$port"
    
    
    
}