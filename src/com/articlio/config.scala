package com.articlio
import com.typesafe.config.ConfigFactory
import java.io.File

object config {
  val config = ConfigFactory.parseFile(new File("../config/config.json"))

  val ldb = config.getString("locations.ldb")
  val output = config.getString("locations.semantic-output")
  val eLife = config.getString("locations.ready-for-semantic.from-eLife")
  val pdf = config.getString("locations.ready-for-semantic.from-pdf")
}