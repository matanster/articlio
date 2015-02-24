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
  
  def unconditionalCreate(data: DataObject): AccessOrError = {
    data.create match {
      case Ready(runID) => data.access  
      case NotReady => new CreateError("failed unconditionally creating data ${data.toString}")
    }
  }
  
  def getSingleDataAccess(data: DataObject): AccessOrError = {
    val accessOrError = getDataAccess(data: DataObject)
    accessOrError
  } 

  //
  // chooses between two forms of processing this function
  //
  private def getDataAccess(data: DataObject): AccessOrError = {
    data.requestedDataID match {
      case None                => getDataAccessAnyID(data)
      case Some(suppliedRunID) => getDataAccessSpecificID(data, suppliedRunID) 
    }      
  }
  
  // checks whether data already exists. if data doesn't exist yet, attempts to create it.
  // returns: access details for ready data, or None cannot be readied
  private def getDataAccessAnyID(data: DataObject): AccessOrError = {
    
    //
    // tree structure for holding the status of a data dependency tree.
    // this structure is "needed" since we don't memoize/cache the dependencies status, 
    // so it is used to freeze the status as creation unfolds, for accurate 
    // error reporting.
    //
    case class AccessTree(data: DataObject, accessOrError: AccessOrError, children: Option[Seq[AccessTree]] = None) {

      // recursively serialize the error/Ok status of the entire tree. might be a bit ugly for now
      private def doSerialize(accessTree: AccessTree) : String = {
        s"${data.dataType} ${data.dataTopic}: ${accessTree.accessOrError match {
            case accessError: AccessError => s"an ${accessError.getClass} error was encountered: ${accessError.errorDetail}"
            case access: Access => "creation was Ok."
          }} \n ${accessTree.children.isEmpty match {
            case true  => 
            case false => s"Its dependencies were: ${accessTree.children.get.map(child => s"dependency ${doSerialize(child)}\n")}"
            }}"
      }
      def serialize = s"Creating ${doSerialize(this)}"
    }
    
    // build dependencies tree, holding either access or error for each dependency
    def getDeps(data: DataObject) : AccessTree = {
      data.dependsOn.nonEmpty match {
        case true  => AccessTree(data, getDataAccess(data), Some(data.dependsOn.map(dep => getDeps(dep))))
        case false => AccessTree(data, getDataAccess(data), None)
      }
    }
    
    // is entire dependencies tree ready?
    def readyDependencies(accessTree: AccessTree) : Boolean = {
      accessTree.children.isEmpty match {
        case true  => true // no dependencies means all dependencies are ready by void
        case false => accessTree.accessOrError match {
          case access: Access      => !accessTree.children.get.map(dep => readyDependencies(dep)).exists(_ == false)
          case error:  AccessError => false
        } 
      }
    }
    
    data.ReadyState match {
      
      case Ready(dataID) => {  
        logger.write(s"data for ${data.getClass} is ready (data id: $dataID)")
        return data.access
      }
      
      case NotReady => {
        logger.write(s"data for ${data.getClass} is not yet ready... attempting to create it...", Some(ConsoleMirror))
        val dataLogger = new DataLogger(data.dataType, data.dataTopic)
        // no missing dependencies, and own create successful?
        val dependenciesStatus = getDeps(data)
        
        readyDependencies(dependenciesStatus) match {
          case false => {
            //logger.write(s"some dependencies for ${data.getClass} were not met\n" + "")
            logger.write(dependenciesStatus.serialize)
            DepsError("some dependencies for ${data.getClass} were not met") // TODO: log exact details of dependencies tree        
          }
          case true =>
            data.create match { 
              case Ready(createdDataID) => {
                logger.write(s"data for ${data.getClass} now ready (data id: $createdDataID)")
                data.access
              }
              case NotReady => {
                logger.write(dependenciesStatus.serialize)
                CreateError("failed creating data")
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