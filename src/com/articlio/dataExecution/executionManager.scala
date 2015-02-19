package com.articlio.dataExecution

import com.articlio.util.runID
import com.articlio.dataExecution.concrete._

//
// executes data preparation by dependencies
//
class DataExecutionManager extends Execute {

  def getDataAccess(data: Data): Option[Access] = {
    data.dataIDrequested match {
      case None                => getDataAccessAnyID(data)
      case Some(suppliedRunID) => getDataAccessSpecificID(data, suppliedRunID) 
    }      
  }
  
  // returns: access details for ready data,
  //          or None if data is not ready
  private def getDataAccessAnyID(data: Data): Option[Access] = {
    
    data.ReadyState match {
      
      case Ready => {  
        println(s"data for ${data.getClass} is ready")
        return Some(data.access)
      }
      
      case NotReady => {
        println(s"data for ${data.getClass} is not yet ready")
        
        // no missing dependencies, and own create successful?
        dataDependenciesReady(data) match {
          case None =>
            if (data.create == Ready) 
              Some(data.access)
            else 
              None
        
          case Some(error) => None          
        }
      }
    }
  }

  // returns: access details for ready data,
  //          or None if data is not ready
  private def getDataAccessSpecificID(data: Data, suppliedRunID: Long): Option[Access] = {
    
    data.ReadyState match {
      
      case Ready => {  
        println(s"data for ${data.getClass} with ID ${suppliedRunID} is ready")
        return Some(data.access)
      }
      
      case NotReady => {
        println(s"there is no data with ID ${suppliedRunID} for ${data.getClass}")
        return None 
      }
    }
  }
  
  // returns whether all dependency data are ready or not
  private def dataDependenciesReady(dataWrapper: Data) : Option[CreateError] = {
    val depsGet = dataWrapper.dependsOn.map(dep => getDataAccess(dep))
    depsGet.exists(d => d == None) match {
      case false => None
      case true  => Some(CreateError("Some data dependencies were not ready")) // TODO: propagate which ones with their texts
    }
  }
}