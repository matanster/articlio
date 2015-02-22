package controllers

import models._
import play.api._
import play.api.mvc._
import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import play.api.http.MimeTypes

//
// play http client imports
//
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import scala.concurrent.Future

import com.articlio.config

object PdfConvert extends Controller {
  
  def convertAll = Action.async { 
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
        WS.url("http://localhost:3000/all").get().map { response => 
        Ok(response.body)
    }   
  }
  
  def convertSingle(fullPath: String) = Action.async { 
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    WS.url("http://localhost:3000/handleInputFile").withQueryString("localLocation" -> fullPath).get().map { response => 
      Ok(response.body)
    }   
  }
}