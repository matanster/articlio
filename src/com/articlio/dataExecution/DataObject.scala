package com.articlio.dataExecution

import play.api.db.slick._
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import models.Tables._
import models.Tables.{Data => DataRecord}
import com.articlio.storage.Connection
import com.articlio.util.Time._


// Data Object That Needs to be Attempted
abstract class DataObject(val requestedDataID: Option[Long] = None) extends RecordException 
                                                                    with DataExecution { 
  
  //
  // tries a function, and collapses its exception into application type 
  //
  def safeRunCreator(func: => Option[CreateError]): Option[CreateError] = {             // this is function argument "by name passing" 
      try { return func } 
      catch { 
      case anyException : Throwable =>
      recordException(anyException)
      return Some(CreateError(anyException.toString)) }
  } 
  
  def create: ReadyState = { 
    
    def registerDependencies(data: DataObject): Unit = {
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
      creatorserverendtime   = None,
      softwareversion = com.articlio.Version.id))

    println(s"got back data ID $dataID")

    // now try this data's creation function    
    val creationError = safeRunCreator(creator(dataID.get, dataType, dataTopic))
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
      creatorserverendtime   = Some(localNow),
      softwareversion = com.articlio.Version.id)
    ) 
    
    //
    // register dependencies if successful creation, and return
    //
    creationError match {
      case None  => {
        registerDependencies(this)
        Ready(dataID.get)
      }
      case Some(error) => NotReady(Some(error)) 
    }
  } 

  def ReadyState: ReadyState = {
    println("In ReadyState!!!")
    requestedDataID match {
      case Some(dataIDrequested) => ReadyStateSpecific(dataIDrequested)
      case None                  => ReadyStateAny
    }
  }
  
  def ReadyStateSpecific(suppliedRunID: Long): ReadyState = {
    DataRecord.filter(_.dataid === suppliedRunID).filter(_.datatype === this.getClass.getSimpleName).filter(_.datatopic === dataTopic).list.nonEmpty match {
      case true => { 
        // TODO: these two lines showcase redundancy and therefore bug potential:
        //       the overall code currently both propagates the dataID in a return type Ready,
        //       in addition to recording it in this.dataID. 
        //       Probably, the former could be dropped in favor of the latter, across the board.
        dataID = Some(suppliedRunID)
        Ready(dataID.get)
      }
      case false => new NotReady
    }
  } 
  
  def ReadyStateAny(): ReadyState = {
    // TODO: collapse to just one call to the database
    println(s"In ReadyStateAny!! for ${this.getClass.getSimpleName}, $dataTopic")
    println(DataRecord.filter(_.datatype === this.getClass.getSimpleName).filter(_.datatopic === dataTopic).list)
    DataRecord.filter(_.datatype === this.getClass.getSimpleName).filter(_.datatopic === dataTopic).list.nonEmpty match {
      case true => {
        dataID = Some(DataRecord.filter(_.datatype === this.getClass.getSimpleName).filter(_.datatopic === dataTopic).list.head.dataid) 
        Ready(dataID.get)
      }
      case false => new NotReady
    }
  } 

  val dataType = this.getClass.getSimpleName
  
  def dataTopic: String
  
  def creator: (Long, String, String) => Option[CreateError]
    
  def access: Access
  
  def dependsOn: Seq[DataObject]
  
  var dataID: Option[Long] = None // for caching database auto-assigned ID  
  
}

// Attempts to Execute a Data Object and Hold Outcome (hence Representing Final State)
case class AttemptDataObject(data: DataObject) extends DataExecution {
  
  val accessOrError: AccessOrError = getSingleDataAccess(data)

  // carry over all immutables of the original data object relevant to the finalized state
  val dataType: String = data.dataType
  val dataTopic: String = data.dataTopic
  val dataID: Option[Long] = data.dataID

  def humanAccessMessage = accessOrError match { 
    case dataAccessDetail : Access => s"$dataType for $dataTopic is ready."
    case error: CreateError        => s"$dataType for $dataTopic failed to create. Please contact development with all necessary details (url, and description of what you were doing)"
    case error: DataIDNotFound     => s"$dataType for $dataTopic with requested data ID ${data.requestedDataID}, does not exist."
    case unexpectedErrorType : AccessError => s"unexpected access error type while tyring to get $this: $unexpectedErrorType"
  }
  
}



@deprecated("to be removed","")
trait Execute extends RecordException {
  def execute[ExpectedType](func: => ExpectedType): Option[ExpectedType] = { // this form of prototype takes a function by name
    try { return Some(func) } 
      catch { 
        case anyException : Throwable =>
          recordException(anyException)
          return None }
  } 
}

@deprecated("to be removed","")
trait oldResultWrapper extends Execute {
  def resultWrapper(func: => Boolean): ReadyState = { // this form of prototype takes a function by name
    execute(func) match {
      case Some(bool) => bool match {
        case true  => Ready(0L)
        case false => new NotReady
      } case None  => new NotReady
    } 
  } 
}