package com.articlio.storage
import akka.actor.Actor
import models.Tables._
import slick.driver.MySQLDriver.api._
import slick.profile.BasicStreamingAction
import slick.jdbc.meta._
import com.articlio.storage.slickDb._
import slick.jdbc.SimpleJdbcAction
import scala.concurrent.duration.Duration
import scala.concurrent._
import slick.jdbc.JdbcBackend
import scala.util.Success
import scala.util.Failure

trait Connection

object slickDb extends {
  
  val db = Database.forConfig("slickdb") // exposes the slick database handle. automatically connection pooled unless disabled in application.conf.
  
  def dbQuery[T1, T2](query: Query[T1, T2, Seq]) = db.run(query.result) // convenience function for query SQL

  // example function for inquiring JDBC configuration
  def printJDBCconfig = {
    import scala.concurrent.ExecutionContext.Implicits.global
    val getAutoCommit = SimpleDBIO[Boolean](_.connection.getAutoCommit); 
    println(s"autocommit is: ${Await.result(db.run(getAutoCommit), Duration.Inf)}")
  }
}

class OutDB extends Actor {

  // Table write functions
  private def write (data: Seq[MatchesRow]) = {
    println
    println(s"writing ${data.length} records to database")
    println
    db.run(Matches ++= data)
    println(s"done writing ${data.length} records to database")    
  }
  
  var buffer = Seq.empty[MatchesRow]
  
  def flushToDB = {
    println("Flushing bulk run's results to database")
      write(buffer)
      buffer = Seq.empty[MatchesRow]
  }
  
  def addToBuffer (data: Seq[MatchesRow]) = buffer ++= data
  
  private def += (data: MatchesRow) = db.run(Matches += data)
  
  private def createIfNeeded {
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    db.run(slick.jdbc.meta.MTable.getTables("Matches")) map { result => if (result.isEmpty) Matches.schema.create }
  }
  
  private def dropCreate: Unit = { // only called from fire-and-forget for now
    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    println("about to recreate tables")
    db.run(DBIO.seq(Matches.schema.drop, 
                    Data.schema.drop,
                    Datadependencies.schema.drop)).onComplete { _ => 
      db.run(DBIO.seq(Matches.schema.create, 
                      Data.schema.create,
                      Datadependencies.schema.create)).onComplete { 
                        case Success(_) => println("done recreating tables") 
                        case Failure(_) => println("failed recreating tables")
                      }
      }
  }

  def receive = { 
    case "dropCreate" => dropCreate
    
    case "createIfNeeded" => createIfNeeded
    
    //case s: Seq[Match @unchecked] => write(s) // annotating to avoid compilation warning about type erasure here
    case s: Seq[MatchesRow @unchecked] => addToBuffer(s) // annotating to avoid compilation warning about type erasure here, maybe no longer necessary?
    
    case "flushToDB" => flushToDB
    
    case _ => throw new Exception("unexpected actor message type received")
  }
}

trait googleSpreadsheetCreator {
  def withHyperlink(route: String, viewableText: String) : String = {
    val linkUrlBase = "http://ubuntu.local:9000"
    s"""=HYPERLINK("$linkUrlBase/$route","$viewableText")"""
  }
}

object createCSV extends googleSpreadsheetCreator {
  import com.github.tototoshi.csv._ // only good for "small" csv files; https://github.com/tototoshi/scala-csv/issues/11
  
  // this old default value is actually meaningless now that the system is geared for concurrency etc....
  // import scala.concurrent.Future
  // implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  // val defaultDataId = dbExecute(Matches.map(m => m.dataid)) map { _.distinct.sorted(Ordering[Long].reverse).head }
  
  def go(dataID: Long) = {
    val outFile = new java.io.File("out.csv")
    val writer = CSVWriter.open(outFile)

    implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
    dbQuery(Matches.filter(m => m.dataid === dataID)) map { filteredData => 
      filteredData.map(m => 
        List(m.docname, 
             withHyperlink("showOriginal/" + m.dataid.toString.dropRight(4),"view original"),          
             withHyperlink("showExtractFoundation/" + m.dataid.toString.dropRight(4) + s"?dataID=${m.dataid}","view result"),
             m.dataid, m.sentence, m.matchpattern, m.locationactual, m.locationtest, m.fullmatch, m.matchindication)) 
        val data = List(List("Run ID", "", "", "Article", "Sentence", "Pattern", "Location Test", "Location Actual", "Final Match?", "Match Indication") :: filteredData.toList) 
        writer.writeAll(data)
    }
  }
}

object createAnalyticSummary { def go = {} } // TODO: restore the real object below, which worked well, adapting it to slick 3.0.0
                                             // TODO: make sure java.io or the CSV Writer are not blocking, wrap in future if they are
/*
object createAnalyticSummary extends Connection with googleSpreadsheetCreator {
  import com.github.tototoshi.csv._ // only good for "small" csv files; https://github.com/tototoshi/scala-csv/issues/11
  // no longer relevant going for the last id with dataID = Matches.map(m => m.dataid).list.distinct.sorted(Ordering[Long].reverse).head
  def go(dataID: Long) = {
    val outFile = new java.io.File("outAnalytic.csv")
    val writer = CSVWriter.open(outFile)
    
    val matchIndications : Seq[String] = Matches.filter(m => m.dataid === dataID).map(m => m.matchindication).list.distinct 
    val grouped = Matches.filter(m => m.dataid === dataID && m.fullmatch).run
                          .groupBy(f => f.dataid)
    
    def hasLimitationSection(docName: String) =
      Headers.filter(_.docname === docName).filter(h => h.header === "limitation").exists.run match {
      case true => "yes"
      case _ =>    "no"
    }                          
    
    val result : List[List[Any]] = grouped.map { case(docName, matches) =>  
                                     List(withHyperlink("showOriginal/" + docName.dropRight(4), docName), hasLimitationSection("ubuntu-2014-11-21T12:06:51.286Z")) ++
                                         matchIndications.map(i => matches.filter(m => m.matchindication == i).length)}.toList

    val headerRow = List(List("Article", "has limitation section?") ++ matchIndications.toList)
    val output = headerRow ++ result
    writer.writeAll(output)

  }
}
*/