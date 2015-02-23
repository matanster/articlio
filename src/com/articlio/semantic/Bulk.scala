package com.articlio.semantic
import java.io.File
import com.articlio.util.runID
import com.articlio.input.JATS
import com.articlio.ldb
import com.articlio.config
import com.articlio.dataExecution._
import com.articlio.dataExecution.concrete._
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
//import models.Tables._
import com.articlio.storage.Connection

/*
class BulkExecutionManager(dataSeq: Seq[Data]) extends Connection { 

  // build dependencies tree, holding either access or error for each dependency
  
  
  def getDeps(data: Data) : AccessTree = {
    data.dependsOn.nonEmpty match {
      case true  => AccessTree(getDataAccess(data), Some(data.dependsOn.map(dep => getDeps(dep))))
      case false => AccessTree(getDataAccess(data), None)
    }
  }
  

  def processAll {

    
    
    //val executionManager = new DataExecutionManager
    //dataSeq
  }
}*/

//val dataIDs = Bulkdatagroups.filter(_.bulkid === bulkID).list.map(_.dataid)
//dataIDs.map(dataID => Data.filter(_.dataid === dataID))