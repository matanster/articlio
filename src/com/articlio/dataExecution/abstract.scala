package com.articlio.dataExecution

import com.articlio.util.runID

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

abstract class Data extends Access with Execute with RecordException { // TODO: replace prototype types with database derived columns from Slick
  
  /*
   *  following function can be avoided, if the execution manager object will assume responsibility 
   *  for safely running all 'create' methods of concrete classes, as makes sense.
   */
  def resultWrapper(func: => Boolean): ReadyState = { // this form of prototype takes a function by name

    execute(func) match {
      case Some(bool) => bool match {
        case true  => Ready
        case false => NotReady
      } case None  => NotReady
    } 
  } 
   
  def ReadyState: ReadyState
  
  def ReadyState(SpecificRunID: Long): ReadyState
  
  def create: ReadyState
  
  def access: Access
  
  def dependsOn: Seq[Data]
}

abstract class DBdata(topic: String) extends Data with com.articlio.storage.Connection {
  
  import models.Tables.{Data => DataRecord}
  import play.api.db.slick._
  import scala.slick.driver.MySQLDriver.simple._
  import scala.slick.jdbc.meta._
  
  def ReadyState(suppliedRunID: Long): ReadyState = {
    DataRecord.filter(_.dataid === suppliedRunID).filter(_.datatype === this.getClass.getName).filter(_.datatopic === topic).list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
  
  def ReadyState(): ReadyState = {
    DataRecord.filter(_.datatype === this.getClass.getName).filter(_.datatopic === topic).list.nonEmpty match {
      case true => Ready
      case false => NotReady
    }
  } 
}
