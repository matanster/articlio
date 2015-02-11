package com.articlio.dataExecution

sealed abstract class ReadyState
object Ready extends ReadyState 
object NotReady extends ReadyState

abstract class Access 

trait RecordException {
  def recordException(exception: Throwable) {
    println(s"exception message: ${exception.getMessage}")
    println(s"exception cause  : ${exception.getCause}")
    println(s"exception class  : ${exception.getClass}")
    println(s"exception stack trace:\n ${exception.getStackTrace.toList.mkString("\n")}")
  }
}

trait Execute extends RecordException {
  def execute[ExpectedType](func: => ExpectedType): Option[ExpectedType] = { // this form of prototype takes a function by name
    try { return Some(func) } 
      catch { 
        case anyException : Throwable =>
          recordException(anyException)
          return None }
  } 
}

abstract class DataWrapper extends Access with Execute with RecordException {

  def wrapper(func: => Boolean): ReadyState = { // this form of prototype takes a function by name

    execute(func) match {
      case Some(bool) => bool match {
        case true => Ready
        case false => NotReady
      }
      case None => NotReady
    } 
  } 
   
  def ReadyState: ReadyState
  
  def create: ReadyState
  
  def access: Access
  
  def dependsOn: Seq[DataWrapper]
}

