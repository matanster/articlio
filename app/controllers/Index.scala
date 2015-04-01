package controllers

import play.api.{db => _, _}
import play.api.mvc._
import models.Tables._
import com.articlio.Globals.db
import slick.driver.MySQLDriver.api._
import play.api.http.MimeTypes
import models.Tables
import scala.concurrent.{Future, Await}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import com.articlio.dataExecution.FinalData
import com.articlio.dataExecution.AccessOrError
import com.articlio.dataExecution.Access
import com.articlio.config
import com.articlio.storage.ManagedDataFiles._

import com.articlio.test.{TestSpec, TestContainer, Testable}
import com.articlio.test.FutureAdditions._
import scala.util.{Success, Failure}

object index extends Controller {
  def go = Action { implicit request => Ok(s"app is up, got request [$request]") }
}
  
 

