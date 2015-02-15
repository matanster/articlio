package com.articlio.dataExecution

import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import models.Tables._
import models.Tables.{Data => DataRecord}
import com.articlio.storage.Connection

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
  
  def dataType: String
  
  def dataTopic: String
  
  def creator: (Long, String) => Option[CreateError]
      
                                                                                                // TODO: need to switch to UTC time for production
  def create: ReadyState = { 

    // gets the current server time  
    def localNow = new java.sql.Timestamp(java.util.Calendar.getInstance.getTime.getTime) // follows from http://alvinalexander.com/java/java-timestamp-example-current-time-now
    
    //
    // tries a function, and collapses its exception into application type 
    //
    def safeRunCreator(func: => Option[CreateError]): Option[CreateError] = {             // this is function argument "by name passing" 
        try { return func } 
        catch { 
        case anyException : Throwable =>
        recordException(anyException)
        return Some(CreateError("exception")) }
    } 
      
    val ownHostName = java.net.InetAddress.getLocalHost.getHostName
    val startTime = localNow

    //
    // register a new data run, and get its unique auto-ID from the RDBMS
    //
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

    // now try this data's creation function    
    val creationError = safeRunCreator(creator(dataID, dataTopic))
      
    // now record the outcome - was the data successfully created by this run?
    DataRecord.filter(_.dataid === dataID).update(DataRow( // cleaner way for only modifying select fields at http://stackoverflow.com/questions/23994003/updating-db-row-scala-slick
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
    
    //
    // return whether the data is now available or not
    //
    creationError match {
      case None  => Ready
      case Some(error) => NotReady 
    }
  } 
  
  def ReadyState(suppliedRunID: Long): ReadyState = {
    DataRecord.filter(_.dataid === suppliedRunID).filter(_.datatype === this.getClass.getName).filter(_.datatopic === dataTopic).list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
  
  def ReadyState(): ReadyState = {
    DataRecord.filter(_.datatype === this.getClass.getName).filter(_.datatopic === dataTopic).list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
  
  def access: Access
  
  def dependsOn: Seq[Data]
  
}




/*****************************
 *                              
 *  potentially dead code              
 * 
 */

trait Execute extends RecordException {
  def execute[ExpectedType](func: => ExpectedType): Option[ExpectedType] = { // this form of prototype takes a function by name
    try { return Some(func) } 
      catch { 
        case anyException : Throwable =>
          recordException(anyException)
          return None }
  } 
}

trait oldResultWrapper extends Execute {
  def resultWrapper(func: => Boolean): ReadyState = { // this form of prototype takes a function by name
    execute(func) match {
      case Some(bool) => bool match {
        case true  => Ready
        case false => NotReady
      } case None  => NotReady
    } 
  } 
}