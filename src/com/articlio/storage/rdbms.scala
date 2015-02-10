package com.articlio.storage
import akka.actor.Actor

//import language.experimental.macros
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
//import scala.slick.lifted._ // more clumsy to debug/use than "direct/simple"
//import scala.slick.direct._ // http://slick.typesafe.com/doc/2.1.0/direct-embedding.html

// direct SQL access imports
//import scala.slick.driver.JdbcDriver.backend.Database
//import Database.dynamicSession
//import scala.slick.jdbc.{GetResult, StaticQuery}

/*
 *
 * Note: to reuse these definitions for database access on the REPL, use e.g. 
 * 
 * import...
 * object a extends Match with Connection { println(matches.map(m => m.runID).list.distinct.sorted(Ordering[String].reverse).mkString("\n")) }; a;
 * 
 */

trait Match {
  type Match = (String, String, String, String, String, String, Boolean, String)

  // slick class binding definition
  class Matches(tag: Tag) extends Table[Match](tag, "Matches") {
    def docName = column[String]("docName")
    def runID = column[String]("runID")
    def sentence = column[String]("sentence", O.Length(20000,varying=true) /*, O.DBType("binary") */)
    def matchPattern = column[String]("matchPattern")
    def locationActual = column[String]("locationActual")
    def locationTest = column[String]("locationTest")    
    def isFinalMatch = column[Boolean]("fullMatch")
    def matchIndication = column[String]("matchIndication")
    def * = (runID, docName, sentence, matchPattern, locationTest, locationActual, isFinalMatch, matchIndication)
  }
  
  // the table handle
  val matches = TableQuery[Matches]
}

trait Connection {
  // connection parameters
  val host     = "localhost"
  val port     = "3306"
  val database = "articlio"
  val user     = "articlio"

  // acquire single database connection used as implicit throughout this object
  println("starting output DB connection...")
  val db = Database.forURL(s"jdbc:mysql://$host:$port/$database", user, driver = "com.mysql.jdbc.Driver")
  implicit val session: Session = db.createSession
}

class OutDB extends Actor with Connection with Match {

  // Table write functions
  private def write (data: Seq[Match]) = {
    println
    println(s"writing ${data.length} records to database")
    println
    matches ++= data
    println(s"done writing ${data.length} records to database")    
  }
  
  var buffer = Seq.empty[Match]
  
  def flushToDB = {
    println("Flushing bulk run's results to database")
      write(buffer)
      buffer = Seq.empty[Match]
  }
  
  def addToBuffer (data: Seq[Match]) = buffer ++= data
  
  private def += (data: Match) = matches += data
  
  private def ++= (data: Seq[String]) = println("stringgggggggggggggg")
  
  private def createIfNeeded {
    if (MTable.getTables("Matches").list.isEmpty) 
      matches.ddl.create
  }
  
  private def dropCreate {
    try {
      matches.ddl.drop 
      println("existing table dropped")
    } catch { case e: Exception => } // exception type not documented
    println("creating table")
    matches.ddl.create
  }

  private def close = session.close
  
  //matches += ("something", "matches something", "indicates something")   
  //matches ++= Seq(("something new", "matches something new", "indicates something"),
  //                ("something new", "matches something new", "indicates something"))
  
  def receive = { 
    case "dropCreate" => dropCreate
    case "createIfNeeded" => createIfNeeded
    //case s: Seq[Match @unchecked] => write(s) // annotating to avoid compilation warning about type erasure here
    case s: Seq[Match @unchecked] => addToBuffer(s) // annotating to avoid compilation warning about type erasure here
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

object createCSV extends Connection with Match with googleSpreadsheetCreator {
  import com.github.tototoshi.csv._ // only good for "small" csv files; https://github.com/tototoshi/scala-csv/issues/11
  def go(runID: String = matches.map(m => m.runID).list.distinct.sorted(Ordering[String].reverse).head) = {
    val outFile = new java.io.File("out.csv")
    val writer = CSVWriter.open(outFile)

    val filteredData = matches.filter(m => m.runID === runID).list.map(m => 
    List(m._1, 
        withHyperlink("showOriginal/" + m._2.dropRight(4),"view original"),          
        withHyperlink("showExtractFoundation/" + m._2.dropRight(4) + s"?runID=${m._1}","view result"),
        m._2, m._3, m._4, m._5, m._6, m._7, m._8))
        val data = List("Run ID", "", "", "Article", "Sentence", "Pattern", "Location Test", "Location Actual", "Final Match?", "Match Indication") :: filteredData 
        writer.writeAll(data)
  }
}

import models.Tables
object createAnalyticSummary extends Connection with Match with Tables with googleSpreadsheetCreator {
  import com.github.tototoshi.csv._ // only good for "small" csv files; https://github.com/tototoshi/scala-csv/issues/11
  def go(runID: String = matches.map(m => m.runID).list.distinct.sorted(Ordering[String].reverse).head) = {
    val outFile = new java.io.File("outAnalytic.csv")
    val writer = CSVWriter.open(outFile)
    
    val matchIndications : Seq[String] = matches.filter(m => m.runID === runID).map(m => m.matchIndication).list.distinct 
    val grouped = matches.filter(m => m.runID === runID && m.isFinalMatch).run
                          .groupBy(f => f._2)
    
    def hasLimitationSection(docName: String) =
      Headers.filter(_.docname === docName).filter(h => h.header === "limitation").exists.run match {
      case true => "yes"
      case _ =>    "no"
    }                          
                          
    val result : List[List[Any]] = grouped.map { case(docName, matches) =>  
                                     List(withHyperlink("showOriginal/" + docName.dropRight(4), docName), hasLimitationSection("ubuntu-2014-11-21T12:06:51.286Z")) ++
                                         matchIndications.map(i => matches.filter(m => m._8 == i).length)}.toList

    val headerRow = List(List("Article", "has limitation section?") ++ matchIndications.toList)
    val output = headerRow ++ result
    writer.writeAll(output)

  }
}

