package com.articlio.ldb

import java.io.File
import com.articlio.util.runID
import com.articlio.JATSprocessing
import com.articlio.ldb
import com.articlio.config
import com.articlio.dataExecution._
import com.articlio.dataExecution.concrete._
import slick.driver.MySQLDriver.api._
import slick.jdbc.meta._
import models.Tables._
import com.articlio.storage.slickDb._
import com.articlio.storage.Connection

//
// Re-create all data belonging to given bulk id
//
object BulkSemanticRecreate extends Connection {
  def buildRequest(bulkID: Long) {
    val ldb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"  

    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    dbQuery(for {
      bulkGroup <- Bulkdatagroups if bulkGroup.bulkid === bulkID
      data <- Data if data.dataid === bulkGroup.dataid 
    } yield data.datatopic) map { articleNames =>
      
        val ldbData = LDBData(ldb)
        val data = articleNames.map(articleName => SemanticData(articleName.toString)(LDB = ldbData))
        val executionManager = new BulkExecutionManager(data, Seq(ldbData))
      
    }
  }
}   

//
// bulk run from a directory
//
object BulkSemanticArbitrary {
  def buildRequest(dirName: String) { 
    // Generate a list of articles to run on - from file names in the input directory
    val ldb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"  
    val files = new File(dirName).listFiles.filter(file => (file.isFile)) // TODO: remove file name's type suffix
    val articleNames = files.map(_.getName)
    
    val ldbData = LDBData(ldb)
    val data = articleNames.map(articleName => SemanticData(articleName)(LDB = ldbData))
    
    val executionManager = new BulkExecutionManager(data, Seq(ldbData))
  }
}   

//
// Receives a sequence of data objects, and parallelizes their execution after verifying their shared dependencies.
// Shared dependencies are provided by the caller.
//
class BulkExecutionManager(dataSeq: Seq[DataObject], sharedDeps: Seq[DataObject]) extends Connection {
  sharedDeps.map(sharedDep => FinalData(sharedDep)).forall(_.accessOrError.isInstanceOf[Access]) match {
    case false => DepsError("bulk run aborted as one or more shared dependencies failed.")
    case true  => dataSeq.par.map(data => FinalData(data))
  }
}

//   val dataIDs = Bulkdatagroups.filter(_.bulkid === bulkID).list.map(_.dataid)
//   val articleNames = dataIDs.map(dataID => DataRecord.filter(_.dataid === dataID).map(_.datatopic))