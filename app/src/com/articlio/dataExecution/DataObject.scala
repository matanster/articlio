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
                .filter(data => data.creationstatus === creationStatusDBtoken.SUCCESS || data.creationstatus === creationStatusDBtoken.STARTED)
    
    val data = db.query(query)
    
    def get(status: String) =
      data map { result => result.filter(data => data.creationstatus == status) } 
    
    get(creationStatusDBtoken.SUCCESS) flatMap { result => 
      result.nonEmpty match {
        case true =>
        {
          dataID complete Success(result.head.dataid)
          Future.successful(Ready(successfullyCompletedID))
        }
        case false =>
          get(creationStatusDBtoken.STARTED) map { result =>
                      result.isEmpty match {
                        case true => new NotReady
                        case false => {
                          println(s"data creation already in progress for $this - waiting for it...")
                          
                          dataID complete Success(result.head.dataid)
                          Ready(result.head.dataid)
                        }
                      }
                   }
      }
    }
  } 

  val dataType = this.getClass.getSimpleName
  
  def dataTopic: String
  
  def creator(dataID: Long, dataTopic: String, articleName: String) : Future[Option[CreateError]]
    
  def access: Access
  
  def dependsOn: Seq[DataObject]
  
  var dataID = Promise[Long] // for caching database auto-assigned ID  
  
  def successfullyCompletedID = dataID.future.value.get.get
  
  // recursively serialize the error/Ok status of the entire tree - if this function is still needed 
  // Note: assumes children's future sequence is already completed when being called
  private def doSerialize(executionTree: ExecutedData): String = {
    
    val children = executionTree.children.value.get.get // extract the sequence from the (assumed to be completed) Future
    
    // maybe a bit ugly string composition, comprising nested formatting strings.
    s"${executionTree.accessOrError match {
        case access:      Access      => "created Ok, "
        case accessError: AccessError => accessError.errorDetail 
       }}${children.isEmpty match { 
            case true  => " (had no dependencies)."
            case false => s". data dependencies were: ${children.map(child =>
                         s"\ndependency ${child.data} - ${doSerialize(child)}")}"
          }
    }"
  }
  
  def serialize(executionTree: ExecutedData) = doSerialize(executionTree: ExecutedData) // (this)  
}

//
// Attempts to Execute a Data Object and Hold Outcome (hence Representing Final State)
//
class FinalData(data: DataObject, val accessOrError: AccessOrError) extends DataExecution {
  
  // carry over all immutables of the original data object relevant to the finalized state
  val dataType: String = data.dataType
  val dataTopic: String = data.dataTopic
      
  val dataID = data.dataID
  
  println(s"In FinalData class")
  
  def humanAccessMessage = {
    accessOrError match { 
      case dataAccessDetail: Access => s"$dataType for $dataTopic is ready."
      case error: CreateError       => s"$dataType for $dataTopic failed to create. Please contact development with all necessary details (url, and description of what you were doing)"
      case error: DataIDNotFound    => s"$dataType for $dataTopic with requested data ID ${data.requestedDataID}, does not exist."
      case error: DepsError         => s"$dataType for $dataTopic failed to create because one or more dependencies were not met: ${error.errorDetail}"
      case unexpectedErrorType : AccessError => s"unexpected access error type while tyring to get $this: $unexpectedErrorType"
      case _ => s"error: unexpected match type ${accessOrError.getClass}"
    }
  }
}

object FinalData extends DataExecution {
  def apply(data: DataObject): Future[FinalData] = {
    targetDataGet(data) map { accessOrError =>
      println(s"In FinalData: $accessOrError")
      new FinalData(data, accessOrError) }
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

@deprecated("", "") case class FinalDataOld(data: DataObject) extends DataExecution {
  
  // carry over all immutables of the original data object relevant to the finalized state
  val dataType: String = data.dataType
  val dataTopic: String = data.dataTopic
      
  val accessOrError: Future[AccessOrError] = targetDataGet(data)

  val dataID: Future[Long] = accessOrError map { _ => data.successfullyCompletedID } // ugly way of getting the data ID out of the system.
  
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
