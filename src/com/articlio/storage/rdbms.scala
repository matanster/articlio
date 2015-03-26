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
    import scala.concurrent.ExecutionContext.Implicits.global
    val getAutoCommit = SimpleDBIO[Boolean](_.connection.getAutoCommit); 
    println(s"autocommit is: ${Await.result(db.run(getAutoCommit), Duration.Inf)}")
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

class OutDB(implicit dbHandle: SlickDB) extends Actor {

  //
  // Table write functions
  //
  
  private def write (data: Seq[MatchesRow]) = {
    println
    println(s"writing ${data.length} records to database")
    println
    dbHandle.run(Matches ++= data)
    println(s"done writing ${data.length} records to database")    
  }
  
  var buffer = Seq.empty[MatchesRow]
  def addToBuffer (data: Seq[MatchesRow]) = buffer ++= data
  def flushToDB = {
    println("Flushing bulk run's results to database")
      write(buffer)
      buffer = Seq.empty[MatchesRow]
  }
 
  private def += (data: MatchesRow) = dbHandle.run(Matches += data) // writes just one row
  
  //
  // Schema handling functions
  //

  val tables = Seq(Matches, Data, Datadependencies)
  
  private def create = {
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    dbHandle.run(DBIO.sequence(tables.map(table => table.schema.create))) 
  }
  
  private def dropCreate = { // only called as fire-and-forget for now
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    println("about to recreate tables")
    dbHandle.run(DBIO.sequence(tables.map(table => table.schema.drop))).onComplete { _ => 
                            create.onComplete { 
                              case Success(s) => println("done recreating tables"); Success(s)  
                              case Failure(f) => println(s"failed recreating tables: $f"); Failure(f)
                            }
      }
  }
  
  private def createIfNeeded {
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
      dbHandle.run(DBIO.sequence(tables.map(table => slick.jdbc.meta.MTable.getTables(table.baseTableRow.tableName) map { 
        result => if (result.isEmpty) {
          println(s"creating table ${table.baseTableRow.tableName}")
          dbHandle.run(table.schema.create) 
        }})))
  }

  /*
   * Actor interface (we don't really need an actor actually) 
   */
  
  def receive = { 
    case "dropCreate" => dropCreate
    
    case "createIfNeeded" => createIfNeeded
    
    case s: Seq[MatchesRow @unchecked] => write(s) // annotating to avoid compilation warning about type erasure here
    //case s: Seq[MatchesRow @unchecked] => addToBuffer(s) // annotating to avoid compilation warning about type erasure here, maybe no longer necessary?
    
    case "flushToDB" => flushToDB
    
    case _ => throw new Exception("unexpected actor message type received")
  }
}

trait Connection // to be merged with object slickDb
