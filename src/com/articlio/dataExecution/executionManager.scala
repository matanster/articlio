package com.articlio.dataExecution

import com.articlio.dataExecution.concrete._
import models.Tables._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import com.articlio.storage.Connection

//
// executes data preparation by dependencies
//
class DataExecutionManager extends Connection {

  def getFinalDataAccess(data: Data): AccessOrError = {
    val access = getDataAccess(data: Data)
    access
  } 
    
  //
  // chooses between two forms of processing this function
  //
  private def getDataAccess(data: Data): AccessOrError = {
    data.requestedDataID match {
      case None                => getDataAccessAnyID(data)
      case Some(suppliedRunID) => getDataAccessSpecificID(data, suppliedRunID) 
    }      
  }
  
  // checks whether data already exists. if data doesn't exist yet, attempts to create it.
  // returns: access details for ready data, or None cannot be readied
  private def getDataAccessAnyID(data: Data): AccessOrError = {
    
    case class AccessTree(accessOrError: AccessOrError, children: Option[Seq[AccessTree]] = None)  
    
    // build dependencies tree, holding either access or error for each dependency
    def getDeps(data: Data) : AccessTree = {
      data.dependsOn.nonEmpty match {
        case true  => AccessTree(getDataAccess(data), Some(data.dependsOn.map(dep => getDeps(dep))))
        case false => AccessTree(getDataAccess(data), None)
      }
    }
    
    // is entire dependencies tree ready?
    def readyDependencies(accessTree: AccessTree) : Boolean = {
      accessTree.children.nonEmpty match {
        case false => true // no dependencies means all dependencies are ready by void
        case true  => accessTree.accessOrError match {
          case access: Access      => !accessTree.children.get.map(dep => readyDependencies(dep)).exists(_ == false)
          case error:  AccessError => false
        } 
      }
    }
    
    data.ReadyState match {
      
      case Ready(dataID) => {  
        println(s"data for ${data.getClass} is ready")
        return data.access
      }
      
      case NotReady => {
        println(s"data for ${data.getClass} is not yet ready... attempting to create it...")
        
        // no missing dependencies, and own create successful?
        val dependenciesStatus = getDeps(data)
        
        readyDependencies(dependenciesStatus) match {
          case false => DepsError("some dependencies were not met") // TODO: log exact details of dependencies tree        
          case true =>
            data.create match { 
              case Ready(createdDataID) => {
                println(s"data for ${data.getClass} now ready")
                data.access
              }
              case NotReady => CreateError("failed creating data")
            }
        }
      }
    }
  }
  
  // checks whether data with specific ID already exists, but doesn't attempt to create it.
  // returns: access details for ready data, or None if data is not ready
  private def getDataAccessSpecificID(data: Data, suppliedRunID: Long): AccessOrError = {
    
    data.ReadyState match {
      
      case Ready(dataID) => {  
        println(s"data for ${data.getClass} with ID ${suppliedRunID} is ready")
        return data.access
      }
      
      case NotReady => {
        val error = s"there is no data with ID ${suppliedRunID} for ${data.getClass}"
        println(error)
        return DataIDNotFound(error)  
      }
    }
  }
}