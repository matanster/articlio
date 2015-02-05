package com.articlio.dataExecution

object ExecutionManager {
  def processRequest(dw: DataWrapper) : Option[Data] = {
    if (dw.isReady) 
      return None
    else 
      try { 
        val data = dw.create 
        return Some(data)
        } catch { case _ : Throwable => None}
  }
}