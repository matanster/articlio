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

  val ldb = config.getString("locations.ldb").rooted
  val output = dataFilesRoot + config.getString("locations.semantic-logging").rooted
  val eLife = dataFilesRoot + config.getString("locations.JATS").rooted
  val pdf = dataFilesRoot + config.getString("locations.JATS").rooted

  val pdfAsJATS = dataFilesRoot + config.getString("locations.pdf-source-extraction.JATS").rooted
  val copyTo = dataFilesRoot + config.getString("locations.JATS").rooted  
  val asText = dataFilesRoot + config.getString("locations.pdf-source-extraction.Text").rooted
  val asEscapedText = dataFilesRoot + config.getString("locations.pdf-source-extraction.EscapedText").rooted
  
  val JATSinput = dataFilesRoot + config.getString("locations.JATS-input.input").rooted
  val JATSformatted = dataFilesRoot + config.getString("locations.JATS-input.formatted").rooted
  val JATSstyled = dataFilesRoot + config.getString("locations.JATS-input.styled").rooted
  val JATSout = dataFilesRoot + config.getString("locations.JATS").rooted
  
  val nodejsServerUrl = s"http://${config.getString("http-services.pdf-sourceExtractor.host")}:${config.getString("http-services.pdf-sourceExtractor.port")}"
  
  //val pdfSourceDir = config.getString("locations.pdf-source-input")
}