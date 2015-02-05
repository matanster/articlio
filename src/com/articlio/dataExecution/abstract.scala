package com.articlio.dataExecution

abstract trait HowToAccess {}

case class Data(ok: Boolean, data: Any) {
}

abstract trait DataWrapper {
  def isReady: Boolean
  def create:  Data 
}

