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
import models.Tables._
import com.articlio.storage.Connection

//
// Re-create all data belonging to given bulk id
//
object BulkSemanticRecreate extends Connection {
  def buildRequest(bulkID: Long) {
    val ldb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"  

    val articleNames = (for {
      bulkGroup <- Bulkdatagroups if bulkGroup.bulkid === bulkID
      data <- Data if data.dataid === bulkGroup.dataid 
    } yield data.datatopic).list
    
    val ldbData = LDBData(ldb) 
    
    val data = articleNames.map(articleName => new SemanticData(articleName.toString)(LDB = ldbData))
    val executionManager = new BulkExecutionManager(data, Seq(ldbData))
  }
}   

//
// Somewhat arbitrary way of generating a list of articles to run on - from file names in a directory
//
object BulkSemanticArbitrary {
  def buildRequest(dirName: String) {
    val ldb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"  
    val files = new File(dirName).listFiles.filter(file => (file.isFile)) // TODO: remove file name's type suffix
    val articleNames = files.map(_.getName)
    
    val ldbData = LDBData(ldb)
    val data = articleNames.map(articleName => new SemanticData(articleName)(LDB = ldbData))
    
    val executionManager = new BulkExecutionManager(data, Seq(ldbData))
  }
}   
 
class BulkExecutionManager(dataSeq: Seq[DataObject], sharedDeps: Seq[DataObject]) extends Connection {
  val executionManager = new DataExecutionManager
  sharedDeps.map(sharedDep => executionManager.getSingleDataAccess(sharedDep)).forall(_.isInstanceOf[Access]) match {
    case false => DepsError("bulk run aborted as one or more shared dependencies failed.")
    case true  => dataSeq.par.map(data => executionManager.getSingleDataAccess(data))
  }
}


    
//   val dataIDs = Bulkdatagroups.filter(_.bulkid === bulkID).list.map(_.dataid)
//   val articleNames = dataIDs.map(dataID => DataRecord.filter(_.dataid === dataID).map(_.datatopic))