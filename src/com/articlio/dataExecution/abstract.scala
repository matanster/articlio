package com.articlio.dataExecution

abstract class Access

//case class Data(ok: Boolean, data: Any) {}

abstract class DataWrapper {

  def isReady: Boolean
  
  def create: Boolean
  
  def access: Access
  
  def dependsOn: Seq[DataWrapper]
}

