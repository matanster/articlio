package com.articlio.dataExecution

sealed abstract class ReadyState
case class Ready(dataID: Long) extends ReadyState 
case class NotReady(error: Option[CreateError] = None) extends ReadyState

abstract class AccessOrError 
abstract class AccessError extends AccessOrError { val errorDetail: String }
case     class CreateError    (errorDetail: String) extends AccessError 
case     class DepsError      (errorDetail: String) extends AccessError
case     class DataIDNotFound (errorDetail: String) extends AccessError
class    Access(dataID: Option[Long] = None) extends AccessOrError

trait RecordException {
  def recordException(exception: Throwable) {
    println(s"exception message: ${exception.getMessage}")
    println(s"exception cause  : ${exception.getCause}")
    println(s"exception class  : ${exception.getClass}")
    println(s"exception stack trace:\n ${exception.getStackTrace.toList.mkString("\n")}")
  }
}