package com.articlio.dataExecution

import com.articlio.util.runID
import com.articlio.dataExecution.concrete._
import models.Tables._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import com.articlio.storage.Connection

//
// executes data preparation by dependencies
//
class DataExecutionManager extends Execute with Connection {

  //
  // chooses between two forms of processing this function
  //
  def getDataAccess(data: Data): AccessOrError = {
    data.dataIDrequested match {
      case None                => getDataAccessAnyID(data)
      case Some(suppliedRunID) => getDataAccessSpecificID(data, suppliedRunID) 
    }      
  }
  
  // returns: access details for ready data,
  //          or None if data is not ready
  private def getDataAccessAnyID(data: Data): AccessOrError = {
      
    // returns whether all dependency data are ready or not
    def AreDependenciesReady(data: Data) : Option[DepsError] = {
      val depsGet = data.dependsOn.map(dep => {
        val dependencyState = getDataAccess(dep)
        dependencyState
      })
      depsGet.exists(d => d.isInstanceOf[AccessError]) match {
        case false => None // dependencies ready, no error
        case true  => Some(DepsError("Some data dependencies were not ready")) // TODO: propagate which ones with their texts
      }
    }
    
    data.ReadyState match {
      
      case Ready(runID) => {  
        println(s"data for ${data.getClass} is ready")
        return data.access
      }
      
      case NotReady => {
        println(s"data for ${data.getClass} is not yet ready... attempting to create it...")
        
        // no missing dependencies, and own create successful?
        AreDependenciesReady(data) match {
          case Some(error) => DepsError("some dependencies were not met")          
          case None =>
            data.create match { 
              case Ready(runID) => {
                println(s"data for ${data.getClass} now ready") 
                data.access
              }
              case NotReady => CreateError("failed creating data")
            }
        }
      }
    }
  }
  
  // returns: access details for ready data,
  //          or None if data is not ready
  private def getDataAccessSpecificID(data: Data, suppliedRunID: Long): AccessOrError = {
    
    data.ReadyState match {
      
      case Ready(runID) => {  
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