package com.articlio.dataExecution

class ReadyState
object Ready extends ReadyState 
object NotReady extends ReadyState

abstract class Access 

abstract class DataWrapper extends Access {

  def recordException(exception: Throwable) {
    println(s"exception message: ${exception.getMessage}")
    println(s"exception cause  : ${exception.getCause}")
    println(s"exception class  : ${exception.getClass}")
    println(s"exception stack trace:\n ${exception.getStackTraceString}")
  }
  
  def wrapper(func: => Boolean): ReadyState = { // this form of prototype takes a function by name
    try {
      return func match {
        case true => Ready
        case false => NotReady
      } 
    } catch { 
        case anyException : Throwable =>
          recordException(anyException)
          return NotReady }
  } 
  
  def ReadyState: ReadyState
  
  def create: ReadyState
  
  def access: Access
  
  def dependsOn: Seq[DataWrapper]
}

