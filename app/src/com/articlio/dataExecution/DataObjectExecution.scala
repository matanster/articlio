package com.articlio.dataExecution

import com.articlio.dataExecution.concrete._
import models.Tables._
import slick.driver.MySQLDriver.simple._
import slick.jdbc.meta._
import com.articlio.storage.Connection
import com.articlio.logger._
import scala.concurrent.Future
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext
import scala.util.Success
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import akka.util.Timeout

/*
 * Executes data preparation by dependencies. 
 */

trait DataExecution extends Connection {

  val logger = new SimplestLogger("DataExecutionManager")

  def unconditionalCreate(data: DataObject): Future[Unit] = { 
    logger.write(s"=== handling unconditional top-level request for data ${data} ===") //
    // UI fit message: s"attempting to create data for ${data.getClass} regardless of whether such data is already available...")
    createOrWait(data) map { _ => Unit } 
  }
  
  def targetDataGet(data: DataObject): Future[Boolean] = { 
    logger.write(Console.BLUE_B + s"<<< handling top-level request for data ${data}" + Console.RESET) //
    data.ReadyState flatMap { _ match { 
        case Ready(dataID) => {
          logger.write(s"Data ${data.getClass.getSimpleName} for ${data.dataTopic} is already ready" + " >>>") 
          Future { true } 
        }
        case NotReady(_) => {
          getDataAccess(data) map { executedTree => 
            logger.write(s"Creating data ${data.getClass.getSimpleName} for ${data.dataTopic}: " + data.serialize + " >>>") // log the entire execution tree 
            data.getError match { // TODO: rid of match warning
              case None => true
              case Some(DepsError(error)) => false   // TODO: revert to passing the error somewhere error.copy(errorDetail = data.serialize)
              case Some(CreateError(error)) => false // TODO: revert to passing the error somewhereerror.copy(errorDetail = data.serialize)
              case Some(DataIDNotFound(error)) => false
            }
          }
        }
      }
    }
  } 

  //
  // chooses between two forms of processing this function
  //
  private def getDataAccess(data: DataObject): Future[Unit] = { // TODO: rename?
    data.requestedDataID match {
      case None                => getDataAccessAnyID(data) 
      case Some(suppliedRunID) => getDataAccessSpecificID(data, suppliedRunID) map { _ => Unit }// map { id => ExecutedData(data, id) } 
    }
  }
  
  private def createOrWait(data: DataObject): Future[DataObject] = {
    
    import akka.pattern.ask
    import akka.util.Timeout
    import scala.concurrent.duration._

    // jumping through a hoop to get ask's future reply, itself a future (flattening it into "just" a future)
    val untyped: Future[Any] = ask(com.articlio.Globals.appActorSystem.deduplicator, Get(data))(Timeout(21474835.seconds)) // future for actor's reply
    val retyped: Future[Future[DataObject]] = untyped.mapTo[Future[DataObject]]                                                        // workaround ask's non-type-safe result
    retyped flatMap(identity)                                                                                              // flatten the future of future
  }
  
  def attemptCreate(data: DataObject): Future[DataObject] = {
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
        data.create map { _ match { 
          case Ready(createdDataID) => {
            logger.write(s"data for ${data.getClass} now ready (data id: $createdDataID)")
            //data.dataID complete Success(createdDataID) assigning here seems to have been unnecessary all along? 
            data.error complete Success(None)
            data
          }
          case NotReady(error) => {
            data.error complete Success(Some(CreateError(s"failed creating data: ${error.get.errorDetail}")))
            data
          }
        }}
      }   
    } 
  }
  
  // checks whether data already exists. if data doesn't exist yet, attempts to create it.
  // returns: access details for ready data, or None cannot be readied
  private def getDataAccessAnyID(data: DataObject): Future[Unit] = {
    println(s"in getDataAccessAnyID for $data")
    data.ReadyState flatMap { _ match {
        case Ready(dataID) => {  
          logger.write(s"data for ${data.getClass} is ready (data id: $dataID)")
          Future.successful(Unit)
        }
        
        case NotReady(_) => {
          logger.write(s"data for ${data.getClass} is not yet ready... attempting to create it...")
          createOrWait(data) map { completedData =>
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
        case Ready(dataID) => {  
          logger.write(s"data for ${data.getClass} with id ${suppliedRunID} is ready")
        }
        
        case NotReady(_) => {
          val error = s"there is no data with id ${suppliedRunID} for ${data.getClass}"
          logger.write(error)
          DataIDNotFound(error)  
        }
      }
    }
  }
}