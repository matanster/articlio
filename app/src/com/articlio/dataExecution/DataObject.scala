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

object CreationStatusDBtoken {
  val STARTED = "started" 
  val SUCCESS = "success"
  val FAILED  = "failed" 
}

/*
 *  Data Object representing a wish for Data.
 *  To be fulfilled by either finding satisfiable data through the data catalog, 
 *  or by creating the data, using the object's `create` function.
 */

abstract class DataObject(val requestedDataID: Option[Long] = None) 
                         (implicit db: SlickDB) extends DataExecution with RecordException { 

  val dataType = this.getClass.getSimpleName // concrete class's name
  
  def dataTopic: String
  
  def creator(dataID: Long, dataTopic: String, articleName: String) : Future[Option[CreateError]]
    
  def dependsOn: Seq[DataObject]
  
  private def getAssumingCompleted[T](promise: Promise[T]) = promise.future.value.get.get // TODO: move to util
  
  val dataID = Promise[Long] // to receive a database insert auto-assigned ID  
  def successfullyCompletedID = getAssumingCompleted(dataID)
  
  val error = Promise[Option[AccessError]] // to house an error if object cannot be satisfied
  def getError = getAssumingCompleted(error) 
  
  
  //
  // Tries a function, and collapses its exception into application type - can be replaced by the use of Try
  //
  private def safeRunCreator(func: => Future[Option[CreateError]]): Future[Option[CreateError]] = { // syntax explanation: 
                                                                                                    // this is function passing "by name".
                                                                                                    // the function supplied by caller is passed as is,
                                                                                                    // so that this function can execute it.
      try { func } // TODO: switch to Try so that fatal JVM-wide errors are automatically treated differently 
        catch { 
          case anyException : Throwable =>
          recordException(anyException)
          Future.successful(Some(CreateError(anyException.toString))) 
        }
  } 
  
  //
  // Creates and registers data, using the data object's `create` function
  //
  def createSelf(assignToGroup: Option[Long] = None): Future[ReadyState] = { 
    println(s"in create for $this")

    // Registers data's dependencies
    def registerDependencies(data: DataObject): Unit = {
      data.dependsOn.map(dependedOnData => {
        db.run(Datadependencies += DatadependenciesRow(data.successfullyCompletedID, dependedOnData.successfullyCompletedID))
        registerDependencies(dependedOnData)
      })
    }
    
    def registerAsGroupMember(data: DataObject): Unit = {
      if (assignToGroup != None) {
        println(Console.GREEN_B + "assign to group " + assignToGroup + Console.RESET)
        db.run(Datagroupings += DatagroupingsRow(assignToGroup.get, data.successfullyCompletedID))
      }
    } 
    
    val ownHostName = java.net.InetAddress.getLocalHost.getHostName  // TODO: move to global initialization object of some sort?
    val startTime = localNow  // TODO: refine the time stamp values to sub-second granularity (see https://github.com/tototoshi/slick-joda-mapper if helpful)

    // register a new data run, and get its unique auto-ID from the RDBMS.
    db.run(DataRecord.returning(DataRecord.map(_.dataid)) += 
      DataRow(dataid                 = 0L, // will be auto-generated 
              datatype               = dataType, 
              datatopic              = dataTopic, 
              creationstatus         = CreationStatusDBtoken.STARTED, 
              creationerrordetail    = None,
              creatorserver          = ownHostName,
              creatorserverstarttime = Some(startTime),
              creatorserverendtime   = None,
              softwareversion        = com.articlio.Globals.appActorSystem.ownGitVersion)
    ) flatMap { 
      
      returnedID =>
        
        dataID complete Success(returnedID)
      
        // now try this data's creation function
        safeRunCreator(creator(successfullyCompletedID, dataType, dataTopic)) flatMap { creationError => 
          // now record the outcome - was the data successfully created by this run?
          db.run(DataRecord.filter(_.dataid === returnedID).update( // cleaner way for only modifying select fields at http://stackoverflow.com/questions/23994003/updating-db-row-scala-slick
            DataRow(dataid                 = returnedID,
                    datatype               = dataType, 
                    datatopic              = dataTopic, 
                    creationstatus         = creationError match {
                                               case None =>    CreationStatusDBtoken.SUCCESS
                                               case Some(_) => CreationStatusDBtoken.FAILED}, 
                    creationerrordetail    = creationError match { 
                                               case None => None
                                               case Some(errorDetail) => Some(errorDetail.toString)},
                    creatorserver          = ownHostName,
                    creatorserverstarttime = Some(startTime),
                    creatorserverendtime   = Some(localNow),
                    softwareversion        = com.articlio.Globals.appActorSystem.ownGitVersion)
          )) map { 
            
            _ => 
                      
            // register the dependencies of the newly successfully created data 
            creationError match {
              case None  => {
                registerDependencies(this)
                registerAsGroupMember(this)
                error complete Success(None)
                logger.write(s"data for $dataType now ready (data id: $successfullyCompletedID)")
                Ready
              }
              case Some(createError) => { 
                error complete Success(Some(CreateError(s"failed creating data: ${createError.errorDetail}")))
                NotReady 
              }
            }
          }
        }
      }
  } 

  //
  // Chooses between two forms of downstream processing for checking if the requested data already exists 
  //
  def ReadyState: Future[ReadyState] = {
    requestedDataID match {
      case Some(dataIDrequested) => ReadyStateSpecific(dataIDrequested)
      case None                  => ReadyStateAny
    }
  }
  
  //
  // checks whether data satisfying the filter criteria and a specific data ID exists
  //
  def ReadyStateSpecific(suppliedRunID: Long): Future[ReadyState] = {
    db.query(DataRecord.filter(_.dataid === suppliedRunID)
                       .filter(_.datatype === this.getClass.getSimpleName)
                       .filter(_.datatopic === dataTopic)
                       .filter(_.creationstatus === CreationStatusDBtoken.SUCCESS)) map { 
      result => result.nonEmpty match {
        case true => { 
          error complete Success(None)
          dataID complete Success(suppliedRunID)
          Ready
        }
        case false => NotReady
      }
    }
  } 
  
  //
  // Checks whether data satisfying the filter criteria exists
  //
  def ReadyStateAny(): Future[ReadyState] = {

    def query = 
      DataRecord.filter(_.datatype === this.getClass.getSimpleName)
                .filter(_.datatopic === dataTopic)
                .filter(data => data.creationstatus === CreationStatusDBtoken.SUCCESS)
    
    db.query(query) map { result =>  
      result.nonEmpty match {
        case true =>
        {
          error complete Success(None)
          dataID complete Success(result.head.dataid)
          Ready
        }
        case false => NotReady
      }
    }
  } 
  
  //
  // Creates text describing the status of the entire dependency tree of this data object 
  // Note: assumes dependencies' future sequence is already completed when being called
  //
  private[dataExecution] def serialize: String = { 
    
    // maybe a bit ugly string composition, comprising nested formatting strings.
    s"${getError match {
        case None => "created Ok, "
        case Some(accessError) => accessError.errorDetail 
       }}${dependsOn.isEmpty match { 
            case true  => " (had no dependencies)."
            case false => s". data dependencies were: ${dependsOn.map(child =>
                         s"\ndependency ${child} - ${child.serialize}")}"
          }
    }"
  }
  
  //
  // Creates simple text describing the success/error of the data creation   
  //
  def humanAccessMessage = { 
    getError match { 
      case None => s"$dataType for $dataTopic is ready."
      case Some(CreateError(errorDetail))    => s"$dataType for $dataTopic failed to create. Please contact development with all necessary details (url, and description of what you were doing)"
      case Some(DataIDNotFound(errorDetail)) => s"$dataType for $dataTopic with requested data ID ${requestedDataID}, does not exist."
      case Some(DepsError(errorDetail))      => s"$dataType for $dataTopic failed to create because one or more dependencies were not met: $errorDetail"
    }
  }  
}

//
// Attempts Satisfying the Data Object (creating a derived object representing final state)
//
object FinalData extends DataExecution {
  def apply(data: DataObject, withNewGroupAssignment: Option[Long] = None): Future[FinalData] = {
    topLevelGet(data, withNewGroupAssignment) map { isSuccessful =>
      new FinalData(data, isSuccessful) }
  }
}

//
// Attempted Data's Final State - a bit superfluous having a special object for this maybe -
// but may come handy in future refactoring for cluster execution
//
class FinalData(data: DataObject, val isSuccessful: Boolean) extends DataExecution {
  // carry over all properties of the underlying data object relevant to the finalized state
  val dataType  = data.dataType
  val dataTopic = data.dataTopic

  lazy val dataID    = data.successfullyCompletedID
  lazy val error     = data.getError
  
  lazy val humanAccessMessage = data.humanAccessMessage
}

