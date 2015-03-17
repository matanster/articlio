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

/*
 * Executes data preparation by dependencies. 
 */

trait DataExecution extends Connection {

  val logger = new SimplestLogger("DataExecutionManager")

  //
  // tree structure for holding the status of a data dependency tree.
  // this structure is "needed" since we don't memoize/cache the dependencies status, 
  // so it is used to freeze the status as creation unfolds, for accurate 
  // error reporting.
  //
  case class ExecutedData(data: DataObject, accessOrError: AccessOrError, children: Future[Seq[ExecutedData]] = Future.successful(Seq())) {

    // recursively serialize the error/Ok status of the entire tree. maybe a bit ugly for now, comprising nested formatting strings.
    // Note: assumes children's future sequence is already completed when deconstructing it
    private def doSerialize(executionTree: ExecutedData): String = {
      implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext   
      
      val children = executionTree.children.value.get.get // extract the sequence from the (assumed completed) Future
      
      s"${executionTree.accessOrError match {
          case access:      Access      => "created Ok,"
          case accessError: AccessError => s"${accessError.errorDetail}"
         }} ${children.nonEmpty match { 
               case true => s"dependencies' details: ${children.map(child =>
                            s"\ndependency ${child.data} - ${doSerialize(child)}")}"
               case false => "had no dependencies"
             }
      }"
    }
    
    def serialize = doSerialize(this)
  }
  
  def unconditionalCreate(data: DataObject): Future[AccessOrError] = { 
    logger.write(s"=== handling unconditional top-level request for data ${data} ===") //
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    // UI fit message: s"attempting to create data for ${data.getClass} regardless of whether such data is already available...")
    attemptCreate(data) map { _.accessOrError} 
  }
  
  def get(data: DataObject): Future[AccessOrError] = { 
    logger.write(s"<<< handling top-level request for data ${data}") //
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    data.ReadyState flatMap { _ match { 
        case Ready(dataID) => {
          logger.write(s"Data ${data.getClass.getSimpleName} for ${data.dataTopic} is already ready" + " >>>") 
          Future { new Access } 
        }
        case NotReady(_) => {
          getDataAccess(data) map { executedTree  => 
          logger.write(s"Creating data ${data.getClass.getSimpleName} for ${data.dataTopic}: " + executedTree.serialize + " >>>") // log the entire execution tree 
          executedTree.accessOrError
          }
        }
      }
    }
  } 

  //
  // chooses between two forms of processing this function
  //
  private def getDataAccess(data: DataObject): Future[ExecutedData] = { // TODO: rename?
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    data.requestedDataID match {
      case None                => getDataAccessAnyID(data) 
      case Some(suppliedRunID) => getDataAccessSpecificID(data, suppliedRunID) map { id => ExecutedData(data, id) } 
    }
  }
  
  private def attemptCreate(data: DataObject): Future[ExecutedData] = {
    println(s"in attemptCreate for $data")
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    // recurse for own dependencies
    val immediateDependencies = Future.sequence(data.dependsOn.map(dep => getDataAccess(dep)))
    
    // is entire dependencies tree ready?
    immediateDependencies map { _.forall(dep => dep.accessOrError.isInstanceOf[Access])} flatMap { _ match {
      case false => {
        //logger.write(s"some dependencies for ${data.getClass.getSimpleName} were not met")
        Future.successful( 
          ExecutedData(data, DepsError(s"some dependencies were not met"), immediateDependencies) // TODO: log exact details of dependencies tree
        )
      }
      case true =>
        data.create map { _ match { 
          case Ready(createdDataID) => {
            logger.write(s"data for ${data.getClass} now ready (data id: $createdDataID)")
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
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    println(s"in getDataAccessAnyID for $data")
    data.ReadyState flatMap { _ match {
        case Ready(dataID) => {  
          logger.write(s"data for ${data.getClass} is ready (data id: $dataID)")
          Future.successful(ExecutedData(data, data.access))
        }
        
        case NotReady(_) => {
          logger.write(s"data for ${data.getClass} is not yet ready... attempting to create it...")
          attemptCreate(data)
        }
      }
    }
  }
  
  // checks whether data with specific ID already exists, but doesn't attempt to create it.
  // returns: access details for ready data, or None if data is not ready
  private def getDataAccessSpecificID(data: DataObject, suppliedRunID: Long)(implicit ec: ExecutionContext): Future[AccessOrError] = {
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