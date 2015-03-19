package com.articlio
import com.typesafe.config.ConfigFactory
import java.io.File

// TODO: leave this object just making the connection to the config file, 
//       letting callers pick their key. that will reduce bloat and make
//       callers more traceable. 

object config {
  
  val config = ConfigFactory.parseFile(new File("../config/config.json"))

  val ldb = config.getString("locations.ldb")
  val output = config.getString("locations.semantic-logging")
  val eLife = config.getString("locations.JATS")
  val pdf = config.getString("locations.JATS")

  val pdfAsJATS = config.getString("locations.pdf-source-extraction.JATS")
  val copyTo = config.getString("locations.JATS")  
  val asText = config.getString("locations.pdf-source-extraction.Text")
  val asEscapedText = config.getString("locations.pdf-source-extraction.EscapedText")
  
  val JATSinput = config.getString("locations.JATS-input.input")
  val JATSformatted = config.getString("locations.JATS-input.formatted")
  val JATSstyled = config.getString("locations.JATS-input.styled")
  val JATSout = config.getString("locations.JATS")
  
  val nodejsServerUrl = s"http://${config.getString("http-services.pdf-sourceExtractor.host")}:${config.getString("http-services.pdf-sourceExtractor.port")}"
  
  //val pdfSourceDir = config.getString("locations.pdf-source-input")
}