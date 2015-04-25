package com.articlio.dataExecution

import com.articlio.dataExecution.concrete._
import models.Tables._
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import com.articlio.storage.Connection
import com.articlio.dataLogger._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import scala.util.Success
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.util.Timeout

/*
 * Trait for Satisfying a Data Object  
 */

trait DataExecution extends Connection { // can refactor to using self type a la `this: DataObject =>` rather than a function argument for the methods. 
                                         // that may also require some common root abstract class to all those that extend this trait.  

  val logger = new SimplestLogger("DataExecutionManager")

  /*
   *  Try to satisfy a data object - ultimately indicating success/failure status through a boolean.
   */
  def topLevelGet(data: DataObject, assignToGroup: Option[Long] = None): Future[Boolean] = { 
    logger.write(Console.BLUE_B + s"<<< handling top-level request for data ${data}" + Console.RESET) //
    data.ReadyState flatMap { _ match { 
        case Ready => {
          logger.write(s"Data ${data.getClass.getSimpleName} for ${data.dataTopic} is already ready" + " >>>")
          Future.successful(true) 
        }
        case NotReady => {
          getDataAccess(data, assignToGroup) map { executedTree => 
            logger.write(s"Creating data ${data.getClass.getSimpleName} for ${data.dataTopic}: " + data.serialize + " >>>") // log the entire execution tree 
            data.getError match { 
              case None => true
              case Some(DepsError(error))      => false // error.copy(errorDetail = data.serialize)
              case Some(CreateError(error))    => false // error.copy(errorDetail = data.serialize)
              case Some(DataIDNotFound(error)) => false
            }
          }
        }
      }
    }
  } 

  //
  // chooses between two forms of downstream processing for getting the requested data 
  //
  private def getDataAccess(data: DataObject, assignToGroup: Option[Long] = None): Future[Unit] = {
    data.requestedDataID match { 
      case None                => getDataAccessAnyID(data, assignToGroup) 
      case Some(suppliedRunID) => getDataAccessSpecificID(data, suppliedRunID) map { _ => Unit }// map { id => ExecutedData(data, id) } 
    }
  }
  
  // checks whether data already exists. if data doesn't exist yet, attempts to create it.
  // returns: access details for ready data, or None cannot be readied
  private def getDataAccessAnyID(data: DataObject, assignToGroup: Option[Long] = None): Future[Unit] = {
    println(s"in getDataAccessAnyID for $data")
    data.ReadyState flatMap { _ match {
        case Ready => {  
          logger.write(s"data for ${data.getClass} is ready (data id: ${data.successfullyCompletedID})")
          Future.successful(Unit)
        }
        
        case NotReady => {
          logger.write(s"data for ${data.getClass} is not yet ready... attempting to create it...")
          createOrWait(data, assignToGroup) map { completedData =>
            // if the dependency was completed by waiting for an equivalent data to complete,
            // then carry over the completed equivalent's completion status.
            if (data ne completedData) {
              data.error  complete Success(completedData.getError)
              data.dataID complete Success(completedData.successfullyCompletedID)
            }
          }
        }
      }
    }
  }
  
  // checks whether data with specific ID already exists, but doesn't attempt to create it.
  // returns: access details for ready data, or None if data is not ready
  private def getDataAccessSpecificID(data: DataObject, suppliedRunID: Long): Future[Unit] = {
    data.ReadyState map { _ match {
        case Ready => {  
          logger.write(s"data for ${data.getClass} with id $suppliedRunID is ready")
        }
        
        case NotReady => {
          val error = s"there is no data with id ${suppliedRunID} for ${data.getClass}"
          logger.write(error)
          DataIDNotFound(error)  
        }
      }
    }
  }
  
  //
  // Passes on to actor that attempts to create the data
  // while collapsing "identical" in-progress data requests 
  // into a single one
  //
  private def createOrWait(data: DataObject, assignToGroup: Option[Long] = None): Future[DataObject] = {
    
    import akka.pattern.ask
    import akka.util.Timeout
    import scala.concurrent.duration._

    // jumping through a hoop to get ask's future reply, itself a future (flattening it into "just" a future)
    val untyped: Future[Any] = ask(com.articlio.Globals.appActorSystem.deduplicator, Get(data, assignToGroup))(Timeout(21474835.seconds)) // future for actor's reply
    val retyped: Future[Future[DataObject]] = untyped.mapTo[Future[DataObject]]                                            // workaround ask's non-type-safe result
    retyped flatMap(identity)                                                                                              // flatten the future of future
  }
  
  //
  // Really attempt to create the data:
  // Attempts all its dependencies (indirectly recursively), first.
  //
  private[dataExecution] def create(data: DataObject, assignToGroup: Option[Long] = None): Future[DataObject] = {
    println(s"in attemptCreate for $data")
    
    // recurse for own dependencies, waiting for them all before passing on
    val dependencies: Future[Seq[Unit]] = Future.sequence(data.dependsOn.map(dep => getDataAccess(dep)))
    
    // is entire dependencies tree ready?
    dependencies map { _ => data.dependsOn.forall(dep => dep.getError == None)} flatMap { _ match {
      case false => {
        Future.successful( 
          data.error complete Success(Some(DepsError(s"some dependencies were not met"))) // retrofit overall to not returning errors within a Success object?
        )
        Future.successful(data)
      }
      case true =>
        data.createSelf(assignToGroup) map { _ => data }
    }}
  }
  
  /*
   *  Try to satisfy a data object by attempting to create its data, regardless of satisfiable data being already available.
   *  May become handy later or for troubleshoot scenarios.
   */
  def unconditionalCreate(data: DataObject): Future[Unit] = { 
    logger.write(s"=== handling unconditional top-level request for data ${data} ===") //
    // UI fit message: s"attempting to create data for ${data.getClass} regardless of whether such data is already available...")
    createOrWait(data) map { _ => Unit } 
  }
}
