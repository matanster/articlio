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
 *  Unlike bulk import, this trait takes care of acting on already-imported data
 */
trait BulkOverData {
  def bulkGo(dataObjects: Seq[DataObject], withNewGroupAssignment: Boolean = true): Future[Seq[FinalData]] = {

    import slick.driver.MySQLDriver.api._
    import com.articlio.Globals.db
    import com.articlio.storage.SlickDB
    import slick.jdbc.meta._
    import models.Tables._
    import play.api.libs.concurrent.Execution.Implicits.defaultContext
    
    println(Console.BLUE_B + "inside bulkGo" + dataObjects + withNewGroupAssignment)
    
    val groupID: Future[Option[Long]] = withNewGroupAssignment match {
      case true  => db.run(Groups.returning(Groups.map(_.groupid)) += GroupsRow(0L)).map(Some(_))
      case false => Future.successful(None)         
    }
    
    groupID flatMap { groupID => println(groupID); Future.sequence(dataObjects.map(dataObject => FinalData(dataObject, groupID))) }
  }
  
  def getGroupData(groupID: Long) = {
    val query = for {
      g <- Datagroupings if g.groupid === groupID
      d <- Data if g.dataid === d.dataid 
    } yield d

    db.query(query) map { result => result.toList.nonEmpty match {
      case true  => result.toList
      case false => println(Console.GREEN_B + "about to throw exception"); throw new Throwable(s"group $groupID does not exist")
    }}
  }
}

object BulkImpl extends BulkOverData {
  
  /* 
   * generate semantic data for every topic included in a group.
   * this can be used for e.g. building up semantic data for a group of imported raw documents,
   * or for a group of 
   */
  def SemanticForGroup(groupID: Long, ldb: LDBData): Future[Seq[FinalData]] = {
    getGroupData(groupID) flatMap { datas => 
      println(datas)
      val a: Seq[DataObject] = datas.map(data => SemanticData(data.datatopic)(LDB = ldb))
      println(Console.BLUE_B + s"Starting to process for group $a")
      bulkGo(a) }
  }  
}





/*
 *  these deprecated functions will cause the shared dependencies to be executed as top data requests.. an undesirable side-effect
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
