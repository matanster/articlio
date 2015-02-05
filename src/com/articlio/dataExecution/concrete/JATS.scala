package com.articlio.dataExecution

import util._
import com.articlio.config
import com.articlio.pipe.pipelines.JATScreateSingle

case class JATS(articleName: String) extends DataWrapper
{
  
  def isReady: Boolean = {
    filePathExists(s"${config.eLife}/$articleName")
  } 
  
  def create : Data = {
    val optimisticResult = new JATScreateSingle(articleName) // TODO: need be a wrapper of it
    return Data(true, Some(optimisticResult))
  }  
  
}
