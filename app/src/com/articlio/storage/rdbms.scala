package com.articlio.storage
import akka.actor.Actor
import models.Tables._
import slick.driver.MySQLDriver.api._
import slick.profile.BasicStreamingAction
import slick.jdbc.meta._
import slick.jdbc.SimpleJdbcAction
import slick.jdbc.JdbcBackend
import models.Tables._
import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.util.{Success, Failure}
//import scala.concurrent.ExecutionContext.Implicits.global
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import com.articlio.logger._, com.articlio.logger.ActiveLogger.logger._

// set log tags
object logTags {
  implicit val tags = Seq(RDBMS)
}; import logTags.tags

//
// Database connection abstract type
//
trait SlickDB {
  val db: slick.driver.MySQLDriver.backend.Database // exposes the slick database handle. 
                                                    // automatically connection pooled unless disabled in application.conf.
  
  def run[R](a: DBIOAction[R, NoStream, Nothing]): Future[R] = db.run(a)
  
  def query[T1, T2](query: Query[T1, T2, Seq]) = db.run(query.result) // convenience function for async query SQL execution
  
  // example function for inquiring JDBC configuration
  def printJDBCconfig = {
    val getAutoCommit = SimpleDBIO[Boolean](_.connection.getAutoCommit); 
    log(s"autocommit is: ${Await.result(db.run(getAutoCommit), Duration.Inf)}")
  }
}

object slickDb extends SlickDB {
  val db = Database.forConfig("slickdb") // exposes the slick database handle. 
      // automatically connection pooled unless disabled in application.conf.
}

object slickTestDb extends SlickDB {
  val db = Database.forConfig("slicktestdb") // exposes the slick database handle. 
                                             // automatically connection pooled unless disabled in application.conf.
}

//
// database write functions - all returning a future so caller can track their completion as appropriate
//
case class OutDB(dbHandle: SlickDB) {

  //
  // Table write functions
  //
  
  def write (data: Seq[MatchesRow]) = {
    log(s"writing ${data.length} records to database")
    dbHandle.run(Matches ++= data)
  }
  
  private def += (data: MatchesRow) = dbHandle.run(Matches += data) // writes just one row
  
  var buffer = Seq.empty[MatchesRow]
  def addToBuffer (data: Seq[MatchesRow]) = buffer ++= data
  def flushToDB = {
    log("Flushing bulk run's results to database")
    write(buffer) map { _ => 
      buffer = Seq.empty[MatchesRow]
    }
  }
  
  //
  // Schema handling functions
  //

  val tables = Seq(Matches, Data, Datadependencies, Groups, Datagroupings)
  
  def create = {
    dbHandle.run(DBIO.sequence(tables.map(table => table.schema.create))) 
  }
  
  def dropCreate: Future[Seq[Unit]] = { 
    log("about to recreate tables", console = true)
    dbHandle.run(DBIO.sequence(tables.map(table => table.schema.drop))) flatMap { _ => create } 
  }
  
  def createIfNeeded {
      dbHandle.run(DBIO.sequence(tables.map(table => slick.jdbc.meta.MTable.getTables(table.baseTableRow.tableName) map { 
        result => if (result.isEmpty) {
          log(s"creating table ${table.baseTableRow.tableName}", console = true)
          dbHandle.run(table.schema.create) 
        }})))
  }
    
}

trait Connection // to be merged with object slickDb
