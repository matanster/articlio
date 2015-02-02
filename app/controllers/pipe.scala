package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import play.api.http.MimeTypes

import com.articlio.pipe.pipelines._
import com.articlio.pipe._

object pipe extends Controller {
   def pipe = Action { implicit request =>
     val eLifeJATSpipeline = new JATSpipeline
     val PDFpipeline = new ConvertedCorpusPipeline
     Ok("Done transforming input files")
  }
}