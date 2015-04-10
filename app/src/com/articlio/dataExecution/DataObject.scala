package com.articlio.dataExecution

//import play.api.db.slick._ play slick plugin is not yet interoperable with Slick 3.0.0
import slick.driver.MySQLDriver.api._
import com.articlio.Globals.db
import com.articlio.storage.SlickDB
import slick.jdbc.meta._
import models.Tables._
import models.Tables.{Data => DataRecord}
import com.articlio.storage.Connection
import com.articlio.util.Time._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Promise
import scala.util.Success

object creationStatusDBtoken {
  val STARTED = "started" 
  val SUCCESS = "success"
  val FAILED  = "failed" 
}

// Data Object That Needs to be Attempted
abstract class DataObject(val requestedDataID: Option[Long] = None)
                         (implicit db: SlickDB) extends RecordException 
                          with DataExecution { 
  
  //
  // tries a function, and collapses its exception into application type 
  //
  def safeRunCreator(func: => Future[Option[CreateError]]): Future[Option[CreateError]] = { // syntax explanation: 
                                                                                            // this is function passing "by name".
                                                                                            // the function supplied by caller is passed as is,
                                                                                            // so that this function can execute it.
      try { return func } // TODO: why is `return` used here with futures?!
        catch { 
          case anyException : Throwable =>
          recordException(anyException)
          Future.successful(Some(CreateError(anyException.toString))) 
        }
  } 
  
  // TODO: refine the time stamp values to sub-second granularity (see https://github.com/tototoshi/slick-joda-mapper if helpful)
  def create: Future[ReadyState] = { 
    println(s"in create for $this")
    def registerDependencies(data: DataObject): Unit = {
      data.dependsOn.map(dependedOnData => {
        println("data:      " + data)
        println("Dep data : " + dependedOnData.successfullyCompletedID)
        db.run(Datadependencies += DatadependenciesRow(data.successfullyCompletedID, dependedOnData.successfullyCompletedID))
        registerDependencies(dependedOnData)
      })
    }
      
    val ownHostName = java.net.InetAddress.getLocalHost.getHostName // TODO: move to global initialization object of some sort
    val startTime = localNow

    //
    // register a new data run, and get its unique auto-ID from the RDBMS.
    // work to remove the blocking wait here - should be easy by now
    //
    dataID complete Success(Await.result(db.run(DataRecord.returning(DataRecord.map(_.dataid)) += DataRow(
      dataid                 = 0L, // will be auto-generated 
      datatype               = dataType, 
      datatopic              = dataTopic, 
      creationstatus         = creationStatusDBtoken.STARTED, 
      creationerrordetail    = None,
      creatorserver          = ownHostName,
      creatorserverstarttime = Some(startTime),
      creatorserverendtime   = None,
      softwareversion        = com.articlio.Globals.appActorSystem.ownGitVersion)), Duration.Inf))

    // now try this data's creation function
    safeRunCreator(creator(successfullyCompletedID, dataType, dataTopic)) map { creationError => 
      // now record the outcome - was the data successfully created by this run?
      db.run(DataRecord.filter(_.dataid === successfullyCompletedID).update(DataRow( // cleaner way for only modifying select fields at http://stackoverflow.com/questions/23994003/updating-db-row-scala-slick
        dataid                 = successfullyCompletedID,
        datatype               = dataType, 
        datatopic              = dataTopic, 
        creationstatus         = creationError match {
                                   case None => creationStatusDBtoken.SUCCESS
                                   case Some(error) => creationStatusDBtoken.FAILED}, 
        creationerrordetail    = creationError match { // for now redundant, but if CreationError evolves... need to convert to string like so
                                   case None => None
                                   case Some(error) => Some(error.toString)},
        creatorserver          = ownHostName,
        creatorserverstarttime = Some(startTime),
        creatorserverendtime   = Some(localNow),
        softwareversion        = com.articlio.Globals.appActorSystem.ownGitVersion))
      ) 
      
      //
      // register dependencies if successful creation, and return
      //
      creationError match {
        case None  => {
          registerDependencies(this)
          Ready(successfullyCompletedID)
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
    db.query(DataRecord.filter(_.dataid === suppliedRunID)
                       .filter(_.datatype === this.getClass.getSimpleName)
                       .filter(_.datatopic === dataTopic)
                       .filter(_.creationstatus === creationStatusDBtoken.SUCCESS)) map { 
      result => result.nonEmpty match {
        case true => { 
          // TODO: these two lines showcase redundancy and therefore bug potential:
          //       the overall code currently both propagates the dataID in a return type Ready,
          //       in addition to recording it in this.dataID. 
          //       Probably, the former could be dropped in favor of the latter, across the board.
          dataID complete Success(suppliedRunID)
          Ready(suppliedRunID)
        }
        case false => new NotReady
      }
    }
  } 
  
  def ReadyStateAny(): Future[ReadyState] = {
    
    // this code is really more complicated than it could be, because it makes 
    // effort to address the database only once and then work with the result from memory 
    
    def query = 
      DataRecord.filter(_.datatype === this.getClass.getSimpleName)
                .filter(_.datatopic === dataTopic)
                .filter(data => data.creationstatus === creationStatusDBtoken.SUCCESS)
    
    db.query(query) map { result => 
      result.nonEmpty match {
        case true =>
        {
          dataID complete Success(result.head.dataid)
          Ready(successfullyCompletedID)
        }
        case false => new NotReady
      }
    }
  } 

  val dataType = this.getClass.getSimpleName
  
  def dataTopic: String
  
  def creator(dataID: Long, dataTopic: String, articleName: String) : Future[Option[CreateError]]
    
  def dependsOn: Seq[DataObject]
  
  val dataID = Promise[Long] // for caching database auto-assigned ID  
  def successfullyCompletedID = dataID.future.value.get.get
  
  val error = Promise[Option[AccessError]]
  def getError = {
    println(this + ":" + error.isCompleted)   
    error.future.value.get.get
  }
  
  // recursively serialize the error/Ok status of the entire tree - if this function is still needed 
  // Note: assumes children's future sequence is already completed when being called
  private def doSerialize: String = { // TODO: serialize through dependencies rather than the obsolete executionTree construct
    
    // maybe a bit ugly string composition, comprising nested formatting strings.
    s"${getError match {
        case None => "created Ok, "
        case Some(accessError) => accessError.errorDetail 
       }}${dependsOn.isEmpty match { 
            case true  => " (had no dependencies)."
            case false => s". data dependencies were: ${dependsOn.map(child =>
                         s"\ndependency ${child} - ${child.doSerialize}")}"
          }
    }"
  }
  
  def serialize = doSerialize
  
  def humanAccessMessage = { // TODO: rename
    getError match { 
      case None => s"$dataType for $dataTopic is ready."
      case Some(CreateError(errorDetail))    => s"$dataType for $dataTopic failed to create. Please contact development with all necessary details (url, and description of what you were doing)"
      case Some(DataIDNotFound(errorDetail)) => s"$dataType for $dataTopic with requested data ID ${requestedDataID}, does not exist."
      case Some(DepsError(errorDetail))      => s"$dataType for $dataTopic failed to create because one or more dependencies were not met: errorDetail"
    }
  }  
}

//
// Attempts to Execute a Data Object and Hold Outcome (hence Representing Final State)
//
class FinalData(data: DataObject, val finalStatus: Boolean) extends DataExecution {
  
  // carry over all immutables of the original data object relevant to the finalized state
  val dataType: String = data.dataType
  val dataTopic: String = data.dataTopic
      
  val dataID = data.dataID
  
  val getError = data.getError
  
  val humanAccessMessage = data.humanAccessMessage
  
  println(s"In FinalData class")
}

object FinalData extends DataExecution {
  def apply(data: DataObject): Future[FinalData] = {
    targetDataGet(data) map { finalStatus =>
      println(s"In FinalData: $finalStatus")
      new FinalData(data, finalStatus) }
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

