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
  case class ExecutedData(data: DataObject, accessOrError: AccessOrError, children: Seq[ExecutedData] = Seq()) {

    // recursively serialize the error/Ok status of the entire tree. might be a bit ugly for now. (nested formatting strings work great, but arguably less readable).
    private def doSerialize(executionTree: ExecutedData): String = {
      /* s"${executionTree.data.dataType} ${executionTree.data.dataTopic}: */ 
      s"${executionTree.accessOrError match {
          case accessError: AccessError => s"${accessError.errorDetail}"
          case access: Access => "created Ok."
        }}${executionTree.children.isEmpty match {
          case true  => ""
          case false => s" - dependencies' details: ${executionTree.children.map(child => s"\ndependency ${child.data} - ${doSerialize(child)}")}"
          }}"
    }
    
    def serialize = s"${doSerialize(this)}"
  }
  
  def unconditionalCreate(data: DataObject): Future[AccessOrError] = { 
    logger.write(s"=== handling unconditional top-level request for data ${data} ===") //
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    // UI fit message: s"attempting to create data for ${data.getClass} regardless of whether such data is already available...")
    attemptCreate(data) map { _.accessOrError} 
  }
  
  def getSingleDataAccess(data: DataObject): Future[AccessOrError] = { 
    logger.write(s"<<< handling top-level request for data ${data}") //
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    data.ReadyState flatMap { _ match { 
        case Ready(dataID) => {
          logger.write(s"Data ${data.getClass.getSimpleName} for ${data.dataTopic} is already ready" + " >>>") 
          Future { new Access } 
        }
        case NotReady(_) => {
          getDataAccess(data) map { executedTree  => 
          logger.write(s"Creating data ${data.getClass.getSimpleName} for ${data.dataTopic}:" + executedTree.serialize + " >>>") // log the entire execution tree 
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
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    // recurse for own dependencies
      val immediateDependencies = Future.sequence(data.dependsOn.map(dep => getDataAccess(dep)))

      // is entire dependencies tree ready?
      immediateDependencies map { _.forall(dep => dep.accessOrError.isInstanceOf[Access])} map { _ match {
        case false => {
          //logger.write(s"some dependencies for ${data.getClass.getSimpleName} were not met")
          ExecutedData(data, DepsError(s"some dependencies were not met"), Await.result(immediateDependencies, Duration.Inf)) // TODO: log exact details of dependencies tree
        }
        case true =>
          data.create match { 
            case Ready(createdDataID) => {
              logger.write(s"data for ${data.getClass} now ready (data id: $createdDataID)")
              ExecutedData(data, data.access, Await.result(immediateDependencies, Duration.Inf)) 
            }
            case NotReady(error) => {
              ExecutedData(data, CreateError(s"failed creating data: ${error.get.errorDetail}"), Await.result(immediateDependencies, Duration.Inf))
            }
          }
        }   
      } 
  }
  
  // checks whether data already exists. if data doesn't exist yet, attempts to create it.
  // returns: access details for ready data, or None cannot be readied
  private def getDataAccessAnyID(data: DataObject): Future[ExecutedData] = {
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    data.ReadyState flatMap { _ match {
        case Ready(dataID) => {  
          logger.write(s"data for ${data.getClass} is ready (data id: $dataID)")
          return Future { ExecutedData(data, data.access) }
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
  private def getDataAccessSpecificID(data: DataObject, suppliedRunID: Long): Future[AccessOrError] = {
    
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    data.ReadyState map { _ match {
        case Ready(dataID) => {  
          logger.write(s"data for ${data.getClass} with id ${suppliedRunID} is ready")
          return Future { data.access }
        }
        
        case NotReady(_) => {
          val error = s"there is no data with id ${suppliedRunID} for ${data.getClass}"
          logger.write(error)
          return Future { DataIDNotFound(error) }  
        }
      }
    }
  }
}