package com.articlio.dataExecution

sealed abstract class ReadyState
case class Ready(dataID: Long) extends ReadyState // it might be redundant to pass along the dataID, 
                                                  // as it is already stored inside the executed data object.
case class NotReady(error: Option[CreateError] = None) extends ReadyState

abstract class AccessOrError 
abstract class AccessError extends AccessOrError { val errorDetail: String }
case     class CreateError    (errorDetail: String) extends AccessError 
case     class DepsError      (errorDetail: String) extends AccessError
case     class DataIDNotFound (errorDetail: String) extends AccessError
class    Access extends AccessOrError

trait RecordException {
  def recordException(exception: Throwable) {
    println(s"exception message: ${exception.getMessage}")
    println(s"exception cause  : ${exception.getCause}")
    println(s"exception class  : ${exception.getClass}")
    println(s"exception stack trace:\n ${exception.getStackTrace.toList.mkString("\n")}")
  }
}