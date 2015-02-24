package com.articlio.dataExecution

import com.articlio.dataExecution.concrete._
import models.Tables._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import com.articlio.storage.Connection
import com.articlio.logger._

//
// executes data preparation by dependencies
//
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
    private def doSerialize(ExecutionTree: ExecutedData): String = {
      s"${data.dataType} ${data.dataTopic}: ${ExecutionTree.accessOrError match {
          case accessError: AccessError => s"an ${accessError.getClass} error was encountered: ${accessError.errorDetail}"
          case access: Access => "creation was Ok."
        }} \n ${ExecutionTree.children.isEmpty match {
          case true  => 
          case false => s"Its dependencies were: ${ExecutionTree.children.map(child => s"dependency ${doSerialize(child)}\n")}"
          }}"
    }
    def serialize = s"Creating ${doSerialize(this)}"
  }
  
  def unconditionalCreate(data: DataObject): AccessOrError = { // TODO: this is a bug - it won't check dependencies. refactor...
    data.create match {
      case Ready(runID) => data.access  
      case NotReady => new CreateError("failed unconditionally creating data ${data.toString}")
    }
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
  
  // checks whether data already exists. if data doesn't exist yet, attempts to create it.
  // returns: access details for ready data, or None cannot be readied
  private def getDataAccessAnyID(data: DataObject): ExecutedData = {
    
    data.ReadyState match {
      
      case Ready(dataID) => {  
        logger.write(s"data for ${data.getClass} is ready (data id: $dataID)")
        return ExecutedData(data, data.access)
      }
      
      case NotReady => {
        logger.write(s"data for ${data.getClass} is not yet ready... attempting to create it...", Some(ConsoleMirror))
        val dataLogger = new DataLogger(data.dataType, data.dataTopic)

        // recurse for own dependencies
        val immediateDendencies = data.dependsOn.map(dep => getDataAccess(dep)) 

        // is entire dependencies tree ready?
        immediateDendencies.forall(dep => dep.accessOrError.isInstanceOf[Access]) match {
          case false => {
            logger.write(s"some dependencies for ${data.getClass} were not met\n" + "")
            ExecutedData(data, DepsError("some dependencies for ${data.getClass} were not met"), immediateDendencies) // TODO: log exact details of dependencies tree        
          }
          case true =>
            data.create match { 
              case Ready(createdDataID) => {
                logger.write(s"data for ${data.getClass} now ready (data id: $createdDataID)")
                ExecutedData(data, data.access, immediateDendencies)
              }
              case NotReady => {
                ExecutedData(data, CreateError("failed creating data"), immediateDendencies)
              }
            }
        }
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
      
      case NotReady => {
        val error = s"there is no data with id ${suppliedRunID} for ${data.getClass}"
        logger.write(error)
        return DataIDNotFound(error)  
      }
    }
  }
}