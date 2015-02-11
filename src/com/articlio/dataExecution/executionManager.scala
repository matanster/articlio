package com.articlio.dataExecution

import com.articlio.ldb.ldb
import com.articlio.util.runID
import com.articlio.dataExecution.concrete._

//
// executes data preparation by dependencies
//
class DataExecutionManager extends Execute {

  // returns: access details for ready data,
  //          or None if data is not ready
  def getDataAccess(dw: DataWrapper): Option[Access] = {
    
    dw.ReadyState match {
      case Ready => {  
        println(s"data for ${dw.getClass} is ready")
        return Some(dw.access)
      }
      
      case NotReady => {
        println(s"data for ${dw.getClass} is not yet ready")
        
        // no missing dependencies, and own create successful?
        if (dataDependenciesReady(dw)) 
          if (dw.create == Ready) return Some(dw.access)

        // otherwise..
        return None 
      }
    }
  }
  
  // returns whether all dependency data are ready or not
  def dataDependenciesReady(dataWrapper: DataWrapper) : Boolean = {
    val depsGet = dataWrapper.dependsOn.map(dep => getDataAccess(dep))
    !depsGet.exists(d => d == None)
  }

}