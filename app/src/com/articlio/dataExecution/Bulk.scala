package com.articlio.dataExecution

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
import com.articlio.storage.Connection
import com.articlio.Globals.db
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.concurrent.Future

/*
 *  these functions will cause the shared dependencies to be executed as top data requests.. an undesireable side-effect
 *  in current architecture.  
 */

//
// Receives a sequence of data objects, and parallelizes their execution after verifying their shared dependencies.
// Shared dependencies are indicated by the caller (not inferred). 
//
@deprecated("", "")
class BulkExecutionManager(dataSeq: Seq[DataObject], sharedDeps: Seq[DataObject]) extends Connection {
  Future.sequence(sharedDeps.map(FinalData(_))) 
    .map {_.forall(_.error == None) match {
      case false => DepsError("bulk run aborted as one or more shared dependencies failed.")
      case true  => dataSeq.par.map(data => FinalData(data))
    }
  }
}

//
// bulk run from a directory
//
@deprecated("", "")
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



/*
//
// Re-create all data belonging to given bulk id
//
object BulkSemanticRecreate extends Connection {
  def buildRequest(bulkID: Long) {
    val ldb = "Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv"  

    db.query(for {
      bulkGroup <- Bulkdatagroups if bulkGroup.bulkid === bulkID
      data <- Data if data.dataid === bulkGroup.dataid 
    } yield data.datatopic) map { articleNames =>
      
        val ldbData = LDBData(ldb)
        val data = articleNames.map(articleName => SemanticData(articleName.toString)(LDB = ldbData))
        val executionManager = new BulkExecutionManager(data, Seq(ldbData))
      
    }
  }
} 
*/

//   val dataIDs = Bulkdatagroups.filter(_.bulkid === bulkID).list.map(_.dataid)
//   val articleNames = dataIDs.map(dataID => DataRecord.filter(_.dataid === dataID).map(_.datatopic))
