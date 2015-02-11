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

    if (dw.ReadyState == Ready) { // already ready? 
      println(s"data for ${dw.getClass} is ready")
      return Some(dw.access)
    }
    else {
      println(s"data for ${dw.getClass} is not yet ready")
      if (dataDependenciesReady(dw)) // no missing dependencies?
        if (execute(dw.create) == Some(Ready)) return Some(dw.access)
      return None
    }
  }
  
  // returns whether all dependency data are ready or not
  def dataDependenciesReady(dataWrapper: DataWrapper) : Boolean = {
    val depsGet = dataWrapper.dependsOn.map(dep => getDataAccess(dep))
    !depsGet.exists(d => d == None)
  }

}