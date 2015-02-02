package com.articlio
import com.typesafe.config.ConfigFactory
import java.io.File

// TODO: leave this object just making the connection to the config file, 
//       letting callers pick their key. that will reduce bloat and make
//       callers more traceable. Just makes more sense.

object config {
  
  val config = ConfigFactory.parseFile(new File("../config/config.json"))

  val ldb = config.getString("locations.ldb")
  val output = config.getString("locations.semantic-output")
  val eLife = config.getString("locations.ready-for-semantic.from-eLife")
  val pdf = config.getString("locations.ready-for-semantic.from-pdf")

  val pdfAsJATS = config.getString("locations.pdf-extraction.asJATS")
  val copyTo = config.getString("locations.ready-for-semantic.from-pdf")
  val asText = config.getString("locations.pdf-extraction.asText")
  val asEscapedText = config.getString("locations.pdf-extraction.asEscapedText")
  
  val JATSinput = config.getString("locations.JATS-input.input")
  val JATSformatted = config.getString("locations.JATS-input.formatted")
  val JATSstyled = config.getString("locations.JATS-input.styled")
  val JATSout = config.getString("locations.ready-for-semantic.from-eLife")
  
  val pdfSourceDir = config.getString("locations.pdf-input")
}