package com.articlio.dataExecution.concrete
import com.articlio.dataExecution._
import util._
import com.articlio.config
import com.articlio.dataExecution._
import models.Tables
import com.articlio.storage.{Connection}
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import models.Tables.{Data => DataRecord}
import scala.concurrent.Future
import com.articlio.Globals.db
import play.api.libs.concurrent.Execution.Implicits.defaultContext
 
/*
 *  dummy data type for tests
 */

case class DummyWithDuration(dataTopic: String, duration: Long) extends DataObject {

  val dependsOn = Seq()
  
  def creator(runID: Long, dataType: String, fileName: String) : Future[Option[CreateError]] = {
    Future.successful { 
      Thread.sleep(duration)
      None
    }
  }
}

