package com.articlio.dataExecution

trait ReadyState { // well, this is really used like an enumeration
  object Ready extends ReadyState 
  object NotReady extends ReadyState
}

abstract class Access 

abstract class DataWrapper extends Access with ReadyState {

  def wrapper(func: => Boolean): ReadyState = { // this form of prototype takes a function by name
    try {
      return func match {
        case true => Ready
        case false => NotReady
      } 
    } catch { 
        case anyException : Throwable => 
          println(anyException.toString)
          return NotReady }
  } 
  
  def ReadyState: ReadyState
  
  def create: ReadyState
  
  def access: Access
  
  def dependsOn: Seq[DataWrapper]
}

