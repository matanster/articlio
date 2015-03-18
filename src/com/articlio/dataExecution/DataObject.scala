package com.articlio.dataExecution

//import play.api.db.slick._ play slick plugin is not yet interoperable with Slick 3.0.0
import slick.driver.MySQLDriver.api._
import com.articlio.storage.slickDb._
import slick.jdbc.meta._
import models.Tables._
import models.Tables.{Data => DataRecord}
import com.articlio.storage.Connection
import com.articlio.util.Time._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.libs.concurrent.Execution.Implicits.defaultContext

// Data Object That Needs to be Attempted
abstract class DataObject(val requestedDataID: Option[Long] = None) extends RecordException 
                                                                    with DataExecution { 
  
  //
  // tries a function, and collapses its exception into application type 
  //
  def safeRunCreator(func: => Future[Option[CreateError]]): Future[Option[CreateError]] = { // syntax explanation: 
                                                                                            // this is function passing "by name".
                                                                                            // the function supplied by caller is passed as is,
                                                                                            // so that this function can execute it.
      try { return func } 
        catch { 
          case anyException : Throwable =>
          recordException(anyException)
          //implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
          Future.successful(Some(CreateError(anyException.toString))) 
        }
  } 
  
  // TODO: refine the time stamp values to sub-second granularity (see https://github.com/tototoshi/slick-joda-mapper if helpful)
  def create: Future[ReadyState] = { 
    println(s"in create for $this")
    def registerDependencies(data: DataObject): Unit = {
      data.dependsOn.map(dependedOnData => {
        db.run(Datadependencies += DatadependenciesRow(data.dataID.get, dependedOnData.dataID.get))
        registerDependencies(dependedOnData)
      })
    }
      
    val ownHostName = java.net.InetAddress.getLocalHost.getHostName // TODO: move to global initialization object of some sort
    val startTime = localNow

    //
    // register a new data run, and get its unique auto-ID from the RDBMS.
    // should be forgivable blocking the thread for a single database insert for now..
    //
    dataID = Some(Await.result(db.run(DataRecord.returning(DataRecord.map(_.dataid)) += DataRow(
      dataid                 = 0L, // will be auto-generated 
      datatype               = dataType, 
      datatopic              = dataTopic, 
      creationstatus         = "started", 
      creationerrordetail    = None,
      creatorserver          = ownHostName,
      creatorserverstarttime = Some(startTime),
      creatorserverendtime   = None,
      softwareversion = com.articlio.Version.id)), Duration.Inf))

    //println(s"got back data ID $dataID")

    // now try this data's creation function
    safeRunCreator(creator(dataID.get, dataType, dataTopic)) map { creationError => 
      // now record the outcome - was the data successfully created by this run?
      db.run(DataRecord.filter(_.dataid === dataID.get).update(DataRow( // cleaner way for only modifying select fields at http://stackoverflow.com/questions/23994003/updating-db-row-scala-slick
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
        softwareversion = com.articlio.Version.id))
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
  } 

  def ReadyState: Future[ReadyState] = {
    requestedDataID match {
      case Some(dataIDrequested) => ReadyStateSpecific(dataIDrequested)
      case None                  => ReadyStateAny
    }
  }
  
  def ReadyStateSpecific(suppliedRunID: Long): Future[ReadyState] = {
    //implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    dbQuery(DataRecord.filter(_.dataid === suppliedRunID).filter(_.datatype === this.getClass.getSimpleName).filter(_.datatopic === dataTopic)) map { 
      result => result.nonEmpty match {
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
  } 
  
  def ReadyStateAny(): Future[ReadyState] = {
    //implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    dbQuery(DataRecord.filter(_.datatype === this.getClass.getSimpleName).filter(_.datatopic === dataTopic)) map {
      result => result.nonEmpty match {
        case true =>
        {
          dataID = Option(result.head.dataid)
          Ready(dataID.get)
        }
        case false => new NotReady
      }
    }
  } 

  val dataType = this.getClass.getSimpleName
  
  def dataTopic: String
  
  def creator(dataID: Long, dataTopic: String, articleName:String) : Future[Option[CreateError]]
    
  def access: Access
  
  def dependsOn: Seq[DataObject]
  
  var dataID: Option[Long] = None // for caching database auto-assigned ID  
  
}

// Attempts to Execute a Data Object and Hold Outcome (hence Representing Final State)
case class FinalData(data: DataObject) extends DataExecution {
  
  val accessOrError: Future[AccessOrError] = get(data)

  // carry over all immutables of the original data object relevant to the finalized state
  val dataType: String = data.dataType
  val dataTopic: String = data.dataTopic
  val dataID: Option[Long] = data.dataID

  //implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  def humanAccessMessage: Future[String] = {
    accessOrError map { _ match { 
        case dataAccessDetail : Access => s"$dataType for $dataTopic is ready."
        case error: CreateError        => s"$dataType for $dataTopic failed to create. Please contact development with all necessary details (url, and description of what you were doing)"
        case error: DataIDNotFound     => s"$dataType for $dataTopic with requested data ID ${data.requestedDataID}, does not exist."
        case error: DepsError          => s"$dataType for $dataTopic failed to create because one or more dependencies were not met: ${error.errorDetail}"
        case unexpectedErrorType : AccessError => s"unexpected access error type while tyring to get $this: $unexpectedErrorType"
        case _ => s"error: unexpected match type ${accessOrError.getClass}"
      }
    }
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
