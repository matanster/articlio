package com.articlio.dataExecution

import models.Tables._
import models.Tables.{Data => DataRecord}
import com.articlio.storage.Connection
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._

sealed abstract class ReadyState
object Ready extends ReadyState 
object NotReady extends ReadyState

case class CreateError(error: String) 

abstract class Access 

trait RecordException {
  def recordException(exception: Throwable) {
    println(s"exception message: ${exception.getMessage}")
    println(s"exception cause  : ${exception.getCause}")
    println(s"exception class  : ${exception.getClass}")
    println(s"exception stack trace:\n ${exception.getStackTrace.toList.mkString("\n")}")
  }
}

abstract class Data extends Access with Execute with RecordException with Connection { 

  /*
   *  following function can be avoided, if the execution manager object will assume responsibility 
   *  for safely running all 'create' methods of concrete classes, as makes sense.
   */
  def create: ReadyState = { // this form of prototype takes a function by name

    def localNow = new java.sql.Timestamp(java.util.Calendar.getInstance.getTime.getTime) // this follows from http://alvinalexander.com/java/java-timestamp-example-current-time-now
                                                                                          // TODO: need to switch to UTC time for production
    val ownHostName = java.net.InetAddress.getLocalHost.getHostName
    
    val startTime = localNow
     
    val dataID = DataRecord.returning(DataRecord.map(_.dataid)) += DataRow(
      dataid                 = 0L, // will be auto-generated 
      datatype               = dataType, 
      datatopic              = dataTopic, 
      creationstatus         = "started", 
      creationerrordetail    = None,
      creatorserver          = ownHostName,
      creatorserverstarttime = Some(startTime),
      creatorserverendtime   = None,
      datadependedon         = None)

      
    def wrapRunCreator(func: => Option[CreateError]): Option[CreateError] = { // this form of prototype takes a function by name
      try { return func } 
        catch { 
          case anyException : Throwable =>
            recordException(anyException)
            return Some(CreateError("exception")) }
    } 
      
    val creationError = wrapRunCreator(creator(dataID))
      
    DataRecord.filter(_.dataid === dataID).update(DataRow( // alternative way for only modifying select fields at http://stackoverflow.com/questions/23994003/updating-db-row-scala-slick
      dataid                 = dataID,
      datatype               = dataType, 
      datatopic              = dataTopic, 
      creationstatus         = creationError match {
                                 case None => "success"
                                 case Some(error) => "failed"}, 
      creationerrordetail    = creationError match { // for now redundant, but if CreationError evolves... need to convert to string like so
                                 case None => None
                                 case Some(error) => Some(error.toString)},
      creatorserver          = ownHostName,
      creatorserverstarttime = Some(startTime),
      creatorserverendtime   = Some(localNow),
      datadependedon         = None)
    ) 
    
    creationError match {
      case None  => Ready
      case Some(error) => NotReady 
    }
  } 
  
  def ReadyState: ReadyState
  
  def ReadyState(SpecificRunID: Long): ReadyState
  
  def dataType: String
  
  def dataTopic: String
  
  def creator: Long => Option[CreateError]
  
  def access: Access
  
  def dependsOn: Seq[Data]
  
}

abstract class DBdata(topic: String) extends Data with com.articlio.storage.Connection {
  
  import models.Tables.{Data => DataRecord}
  import play.api.db.slick._
  import scala.slick.driver.MySQLDriver.simple._
  import scala.slick.jdbc.meta._
  
  def ReadyState(suppliedRunID: Long): ReadyState = {
    DataRecord.filter(_.dataid === suppliedRunID).filter(_.datatype === this.getClass.getName).filter(_.datatopic === topic).list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
  
  def ReadyState(): ReadyState = {
    DataRecord.filter(_.datatype === this.getClass.getName).filter(_.datatopic === topic).list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
}


trait Execute extends RecordException {
  def execute[ExpectedType](func: => ExpectedType): Option[ExpectedType] = { // this form of prototype takes a function by name
    try { return Some(func) } 
      catch { 
        case anyException : Throwable =>
          recordException(anyException)
          return None }
  } 
}

trait oldResultWrapper {
  def resultWrapper(func: => Boolean): ReadyState = { // this form of prototype takes a function by name
    execute(func) match {
      case Some(bool) => bool match {
        case true  => Ready
        case false => NotReady
      } case None  => NotReady
    } 
  } 
}