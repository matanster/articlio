package com.articlio.dataExecution

import play.api.db.slick._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import models.Tables._
import models.Tables.{Data => DataRecord}
import com.articlio.storage.Connection

sealed abstract class ReadyState
case class Ready(dataID: Long) extends ReadyState 
object NotReady extends ReadyState

abstract class AccessOrError {}
abstract class AccessError    extends AccessOrError
case     class CreateError    (errorDetail: String) extends AccessError 
case     class DepsError      (errorDetail: String) extends AccessError
case     class DataIDNotFound (errorDetail: String) extends AccessError
class    Access extends AccessOrError

trait RecordException {
  def recordException(exception: Throwable) {
    println(s"exception message: ${exception.getMessage}")
    println(s"exception cause  : ${exception.getCause}")
    println(s"exception class  : ${exception.getClass}")
    println(s"exception stack trace:\n ${exception.getStackTrace.toList.mkString("\n")}")
  }
}

abstract class Data(val requestedDataID: Option[Long] = None) extends RecordException with Connection { 
  
  def create: ReadyState = { 

    // gets the current server time  
    def localNow = new java.sql.Timestamp(java.util.Calendar.getInstance.getTime.getTime) // follows from http://alvinalexander.com/java/java-timestamp-example-current-time-now
                                                                                          // TODO: need to switch to UTC time for production
    
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
    
    def registerDependencies(data: Data) : Unit = {
      data.dependsOn.map(dependedOnData => { 
        Datadependencies += DatadependenciesRow(data.dataID.get, dependedOnData.dataID.get)
        registerDependencies(dependedOnData)
      })
    }

      
    val ownHostName = java.net.InetAddress.getLocalHost.getHostName
    val startTime = localNow

    //
    // register a new data run, and get its unique auto-ID from the RDBMS
    //
    dataID = Some(DataRecord.returning(DataRecord.map(_.dataid)) += DataRow(
      dataid                 = 0L, // will be auto-generated 
      datatype               = dataType, 
      datatopic              = dataTopic, 
      creationstatus         = "started", 
      creationerrordetail    = None,
      creatorserver          = ownHostName,
      creatorserverstarttime = Some(startTime),
      creatorserverendtime   = None))

    // now try this data's creation function    
    val creationError = safeRunCreator(creator(dataID.get, dataTopic))
      
    // now record the outcome - was the data successfully created by this run?
    DataRecord.filter(_.dataid === dataID.get).update(DataRow( // cleaner way for only modifying select fields at http://stackoverflow.com/questions/23994003/updating-db-row-scala-slick
      dataid                 = dataID.get,
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
      creatorserverendtime   = Some(localNow))
    ) 
    
    //
    // register dependencies if successful creation, and return
    //
    creationError match {
      case None  => {
        registerDependencies(this)
        Ready(dataID.get)
      }
      case Some(error) => NotReady 
    }
  } 

  def ReadyState: ReadyState = {
    requestedDataID match {
      case Some(dataIDrequested) => ReadyStateSpecific(dataIDrequested)
      case None                  => ReadyStateAny
    }
  }
  
  def ReadyStateSpecific(suppliedRunID: Long): ReadyState = {
    DataRecord.filter(_.dataid === suppliedRunID).filter(_.datatype === this.getClass.getName).filter(_.datatopic === dataTopic).list.nonEmpty match {
      case true => Ready(suppliedRunID)
      case false => NotReady
    }
  } 
  
  def ReadyStateAny(): ReadyState = {
    // TODO: collapse to just one call to the database
    DataRecord.filter(_.datatype === this.getClass.getName).filter(_.datatopic === dataTopic).list.nonEmpty match {
      case true => Ready(DataRecord.filter(_.datatype === this.getClass.getName).filter(_.datatopic === dataTopic).list.head.dataid) 
      case false => NotReady
    }
  } 

  def dataType: String
  
  def dataTopic: String
  
  def creator: (Long, String) => Option[CreateError]
    
  def access: Access
  
  def dependsOn: Seq[Data]
  
  var dataID: Option[Long] = None // for caching database auto-assigned ID  
  
}




@deprecated("to be removed","bla")
trait Execute extends RecordException {
  def execute[ExpectedType](func: => ExpectedType): Option[ExpectedType] = { // this form of prototype takes a function by name
    try { return Some(func) } 
      catch { 
        case anyException : Throwable =>
          recordException(anyException)
          return None }
  } 
}

@deprecated("to be removed","bla")
trait oldResultWrapper extends Execute {
  def resultWrapper(func: => Boolean): ReadyState = { // this form of prototype takes a function by name
    execute(func) match {
      case Some(bool) => bool match {
        case true  => Ready(0L)
        case false => NotReady
      } case None  => NotReady
    } 
  } 
}