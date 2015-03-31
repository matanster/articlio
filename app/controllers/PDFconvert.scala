package controllers

import models._
import play.api._
import play.api.mvc._
//import play.api.db.slick._ play slick plugin is not yet interoperable with Slick 3.0.0, slick is wired in without it
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import play.api.http.MimeTypes
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import scala.concurrent.Future
import com.articlio.config
import scala.util.{Success, Failure}

object PdfConvert extends Controller {
  
  //val nodejsServerUrl = s"http://${config.config.getString("http-services.pdf-sourceExtractor.host")}:${config.config.getString("http-services.pdf-sourceExtractor.port")}"
  
  def convertAll = Action.async { 
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
        WS.url(s"${config.nodejsServerUrl}/all").get().map { response => 
        Ok(response.body)
    }   
  }
  
  def convertSingle(articleName: String) = Action.async { implicit request => 
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    WS.url(s"${config.nodejsServerUrl}/handleInputFile").withQueryString("localLocation" -> articleName).withRequestTimeout(6000).get.map(response => 
      response.status match {
        case 200 => Ok("successfully converted pdf to JATS")
        case _ => InternalServerError("failed converting pdf to JATS")
      })
    }
}