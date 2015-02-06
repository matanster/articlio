package com.articlio.dataExecution

import com.articlio.ldb.ldb
import com.articlio.util.runID
import com.articlio.dataExecution.concrete._

class ExecutionManager {

  def get(dw: DataWrapper): Option[Access] = {
    if (dw.isReady) // already ready? 
      return Some(dw.access)
    else {
      if (depsGet(dw)) { // dependencies ready?
        try { // TODO: re-factor to use function that returns false on either false value or exception
          val data = dw.create
          return data match {
            case true  => Some(dw.access)
            case false => None
          }
        } catch { case _ : Throwable => return None}
      }
      else return None
    }
  }
  
  def depsGet(dataWrapper: DataWrapper) : Boolean = {
    val deps = dataWrapper.dependsOn.map(dep => get(dep))
    !deps.exists (dep => dep.isEmpty) 
  }

}