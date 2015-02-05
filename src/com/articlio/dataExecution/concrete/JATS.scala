package com.articlio.dataExecution

import util._
import com.articlio.config

case class JATSs(articleName: String) extends Data
{
  
  def isReady = {
    filePathExists(s"${config.eLife}/$articleName")
  } 
  
  def Create = {
    controllers.Ldb.singleeLifeSourced(articleName) // TODO: need be a wrapper of it
  }  
  
}
