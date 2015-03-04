package com.articlio.dataExecution

import com.articlio.dataExecution.concrete._
import models.Tables._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import com.articlio.storage.Connection
import com.articlio.logger._

/*
 * Execute data preparation by dependencies. 
 * 
 */

class DataExecutionManager extends Connection {

  val logger = new SimplestLogger("DataExecutionManager")

  //
  // tree structure for holding the status of a data dependency tree.
  // this structure is "needed" since we don't memoize/cache the dependencies status, 
  // so it is used to freeze the status as creation unfolds, for accurate 
  // error reporting.
  //
  case class ExecutedData(data: DataObject, accessOrError: AccessOrError, children: Seq[ExecutedData] = Seq()) {

    // recursively serialize the error/Ok status of the entire tree. might be a bit ugly for now
    private def doSerialize(executionTree: ExecutedData): String = {
      /* s"${executionTree.data.dataType} ${executionTree.data.dataTopic}: */ s"${executionTree.accessOrError match {
          case accessError: AccessError => s"a ${accessError.getClass.getSimpleName} error was encountered: ${accessError.errorDetail}"
          case access: Access => "created Ok."
        }}${executionTree.children.isEmpty match {
          case true  => ""
          case false => s" - dependencies' details: ${executionTree.children.map(child => s"\ndependency ${child.data} - ${doSerialize(child)}")}"
          }}"
    }
    def serialize = s"Creating ${data.getClass.getSimpleName} for ${data.dataTopic}: ${doSerialize(this)}"
  }
  
  def unconditionalCreate(data: DataObject): AccessOrError = { // TODO: this is a bug - it won't check dependencies. refactor...
    logger.write(s"attempting to create data for ${data.getClass} regardless of whether such data is already available...")
    attemptCreate(data).accessOrError 
  }
  
  def getSingleDataAccess(data: DataObject): AccessOrError = {
    val executedTree = getDataAccess(data: DataObject)
    logger.write(executedTree.serialize) // log the entire execution tree 
    executedTree.accessOrError
  } 

  //
  // chooses between two forms of processing this function
  //
  private def getDataAccess(data: DataObject): ExecutedData = {
    data.requestedDataID match {
      case None                => getDataAccessAnyID(data)
      case Some(suppliedRunID) => ExecutedData(data, getDataAccessSpecificID(data, suppliedRunID)) 
    }      
  }
  
  private def attemptCreate(data: DataObject): ExecutedData = {
    // recurse for own dependencies
      val immediateDependencies = data.dependsOn.map(dep => getDataAccess(dep)) 

      // is entire dependencies tree ready?
      immediateDependencies.forall(dep => dep.accessOrError.isInstanceOf[Access]) match {
        case false => {
          logger.write(s"some dependencies for ${data.getClass.getSimpleName} were not met\n" + "")
          ExecutedData(data, DepsError(s"some dependencies were not met"), immediateDependencies) // TODO: log exact details of dependencies tree        
        }
        case true =>
          data.create match { 
            case Ready(createdDataID) => {
              logger.write(s"data for ${data.getClass} now ready (data id: $createdDataID)")
              ExecutedData(data, data.access, immediateDependencies)
            }
            case NotReady(error) => {
              ExecutedData(data, CreateError(s"failed creating data: ${error.get.errorDetail}"), immediateDependencies)
            }
          }
      }  
  }
  
  // checks whether data already exists. if data doesn't exist yet, attempts to create it.
  // returns: access details for ready data, or None cannot be readied
  private def getDataAccessAnyID(data: DataObject): ExecutedData = {
    
    data.ReadyState match {
      case Ready(dataID) => {  
        logger.write(s"data for ${data.getClass} is ready (data id: $dataID)")
        return ExecutedData(data, data.access)
      }
      
      case NotReady(_) => {
        logger.write(s"data for ${data.getClass} is not yet ready... attempting to create it...")
        attemptCreate(data)
      }
    }
  }
  
  // checks whether data with specific ID already exists, but doesn't attempt to create it.
  // returns: access details for ready data, or None if data is not ready
  private def getDataAccessSpecificID(data: DataObject, suppliedRunID: Long): AccessOrError = {
    
    data.ReadyState match {
      case Ready(dataID) => {  
        logger.write(s"data for ${data.getClass} with id ${suppliedRunID} is ready")
        return data.access
      }
      
      case NotReady(_) => {
        val error = s"there is no data with id ${suppliedRunID} for ${data.getClass}"
        logger.write(error)
        return DataIDNotFound(error)  
      }
    }
  }
}