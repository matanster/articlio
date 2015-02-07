package com.articlio.dataExecution

import com.articlio.ldb.ldb
import com.articlio.util.runID
import com.articlio.dataExecution.concrete._

//
// executes data preparation by dependencies
//
class DataExecutionManager extends ReadyState {

  // returns: access details for ready data,
  //          or None if data is not ready
  def getDataAccess(dw: DataWrapper): Option[Access] = {
    if (dw.ReadyState == Ready) // already ready? 
      return Some(dw.access)
    else {
      if (dataDependenciesReady(dw)) { // no missing dependencies?
        try { 
          val data = dw.create
          return data match {
            case Ready  => Some(dw.access)
            case NotReady => None
          }
        } catch { case _ : Throwable => return None}
      }
      else return None
    }
  }
  
  // returns whether all dependency data are ready or not
  def dataDependenciesReady(dataWrapper: DataWrapper) : Boolean = {
    val depsGet = dataWrapper.dependsOn.map(dep => getDataAccess(dep))
    !depsGet.exists(d => d == None)
  }

}