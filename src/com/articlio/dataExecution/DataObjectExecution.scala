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
  
//
// tree structure for holding the status of a data dependency tree.
// this structure is "needed" since we don't memoize/cache the dependencies 
// status, so it is used to freeze the status as creation unfolds, for accurate 
// error reporting. On later review, may be unnecessary a class, if each
// dependency kept its error and remained available.
//
case class ExecutedData(data: DataObject, accessOrError: AccessOrError, children: Future[Seq[ExecutedData]] = Future.successful(Seq())) {

  // recursively serialize the error/Ok status of the entire tree. 
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
  
  def serialize = doSerialize(this)
}

/*
 * Executes data preparation by dependencies. 
 */

trait DataExecution extends Connection {

  val logger = new SimplestLogger("DataExecutionManager")

  def unconditionalCreate(data: DataObject): Future[AccessOrError] = { 
    logger.write(s"=== handling unconditional top-level request for data ${data} ===") //
    // UI fit message: s"attempting to create data for ${data.getClass} regardless of whether such data is already available...")
    createOrWait(data) map { _.accessOrError} 
  }
  
  def targetDataGet(data: DataObject): Future[AccessOrError] = { 
    logger.write(Console.BLUE_B + s"<<< handling top-level request for data ${data}" + Console.RESET) //
    data.ReadyState flatMap { _ match { 
        case Ready(dataID) => {
          logger.write(s"Data ${data.getClass.getSimpleName} for ${data.dataTopic} is already ready" + " >>>") 
          Future { new Access } 
        }
        case NotReady(_) => {
          getDataAccess(data) map { executedTree => 
            logger.write(s"Creating data ${data.getClass.getSimpleName} for ${data.dataTopic}: " + executedTree.serialize + " >>>") // log the entire execution tree 
            executedTree.accessOrError match {
              case access: Access => executedTree.accessOrError 
              case error : DepsError => error.copy(errorDetail = executedTree.serialize)
              case error : CreateError => error.copy(errorDetail = executedTree.serialize)
              case _ => throw new Exception("internal err")
            }
          }
        }
      }
    }
  } 

  //
  // chooses between two forms of processing this function
  //
  private def getDataAccess(data: DataObject): Future[ExecutedData] = { // TODO: rename?
    data.requestedDataID match {
      case None                => getDataAccessAnyID(data) 
      case Some(suppliedRunID) => getDataAccessSpecificID(data, suppliedRunID) map { id => ExecutedData(data, id) } 
    }
  }
  
  private def createOrWait(data: DataObject): Future[ExecutedData] = {
    
    import akka.pattern.ask
    import akka.util.Timeout
    import scala.concurrent.duration._

    // jumping through a hoop to get ask's future reply, itself a future (flattening it into "just" a future)
    val untyped: Future[Any] = ask(com.articlio.Globals.appActorSystem.deduplicator, Get(data))(Timeout(21474835.seconds)) // future for actor's reply
    val retyped: Future[Future[ExecutedData]] = untyped.mapTo[Future[ExecutedData]]                                        // actor's reply is a future of an ExecutedData 
    retyped flatMap(identity)                                                                                              // flatten the future of future
  }
  
  def attemptCreate(data: DataObject): Future[ExecutedData] = {
    println(s"in attemptCreate for $data")
    
    // recurse for own dependencies
    val immediateDependencies = Future.sequence(data.dependsOn.map(dep => getDataAccess(dep)))
    
    // is entire dependencies tree ready?
    immediateDependencies map { _.forall(dep => dep.accessOrError.isInstanceOf[Access])} flatMap { _ match {
      case false => {
        Future.successful( 
          ExecutedData(data, DepsError(s"some dependencies were not met"), immediateDependencies) 
        )
      }
      case true =>
        data.create map { _ match { 
          case Ready(createdDataID) => {
            logger.write(s"data for ${data.getClass} now ready (data id: $createdDataID)")
            data.dataID = Some(createdDataID)
            ExecutedData(data, data.access, immediateDependencies) 
          }
          case NotReady(error) => {
            ExecutedData(data, CreateError(s"failed creating data: ${error.get.errorDetail}"), immediateDependencies)
          }
        }}
      }   
    } 
  }
  
  // checks whether data already exists. if data doesn't exist yet, attempts to create it.
  // returns: access details for ready data, or None cannot be readied
  private def getDataAccessAnyID(data: DataObject): Future[ExecutedData] = {
    println(s"in getDataAccessAnyID for $data")
    data.ReadyState flatMap { _ match {
        case Ready(dataID) => {  
          logger.write(s"data for ${data.getClass} is ready (data id: $dataID)")
          Future.successful(ExecutedData(data, data.access))
        }
        
        case NotReady(_) => {
          logger.write(s"data for ${data.getClass} is not yet ready... attempting to create it...")
          createOrWait(data)
        }
      }
    }
  }
  
  // checks whether data with specific ID already exists, but doesn't attempt to create it.
  // returns: access details for ready data, or None if data is not ready
  private def getDataAccessSpecificID(data: DataObject, suppliedRunID: Long): Future[AccessOrError] = {
    data.ReadyState map { _ match {
        case Ready(dataID) => {  
          logger.write(s"data for ${data.getClass} with id ${suppliedRunID} is ready")
          data.access
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