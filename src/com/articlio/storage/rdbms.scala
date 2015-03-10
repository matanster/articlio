package com.articlio.storage
import akka.actor.Actor

//import language.experimental.macros
import models.Tables._
import slick.driver.MySQLDriver.api._
//import slick.driver.MySQLDriver.simple._
//import slick.jdbc.meta._

object slickDb {
  val db = Database.forConfig("slickdb")
}

trait Connection {
  // connection parameters
  private val host     = "localhost"
  private val port     = "3306"
  private val database = "articlio"
  private val user     = "articlio"

  // acquire single database connection used as implicit throughout this object
  println("starting output DB connection...")
  private val db = Database.forURL(s"jdbc:mysql://$host:$port/$database", user, driver = "com.mysql.jdbc.Driver")
  implicit val session: Session = db.createSession
}

class OutDB extends Actor with Connection {

  // Table write functions
  private def write (data: Seq[MatchesRow]) = {
    println
    println(s"writing ${data.length} records to database")
    println
    Matches ++= data
    println(s"done writing ${data.length} records to database")    
  }
  
  var buffer = Seq.empty[MatchesRow]
  
  def flushToDB = {
    println("Flushing bulk run's results to database")
      write(buffer)
      buffer = Seq.empty[MatchesRow]
  }
  
  def addToBuffer (data: Seq[MatchesRow]) = buffer ++= data
  
  private def += (data: MatchesRow) = Matches += data
  
  private def ++= (data: Seq[String]) = println("stringgggggggggggggg")
  
  private def createIfNeeded {
    if (MTable.getTables("Matches").list.isEmpty) 
      Matches.ddl.create
  }
  
  private def dropCreate {
    try {
      Matches.ddl.drop 
      Data.ddl.drop
      Datadependencies.ddl.drop
      println("existing tables dropped")
    } catch { case e: Exception => } // exception type not documented
    println("creating tables")
    Matches.ddl.create 
    Data.ddl.create
    Datadependencies.ddl.create
  }

  private def close = session.close
  
  //matches += ("something", "matches something", "indicates something")   
  //matches ++= Seq(("something new", "matches something new", "indicates something"),
  //                ("something new", "matches something new", "indicates something"))
  
  def receive = { 
    case "dropCreate" => dropCreate
    case "createIfNeeded" => createIfNeeded
    //case s: Seq[Match @unchecked] => write(s) // annotating to avoid compilation warning about type erasure here
    case s: Seq[MatchesRow @unchecked] => addToBuffer(s) // annotating to avoid compilation warning about type erasure here,
                                                         // maybe no longer necessary?
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

object createCSV extends Connection with googleSpreadsheetCreator {
  import com.github.tototoshi.csv._ // only good for "small" csv files; https://github.com/tototoshi/scala-csv/issues/11
  def go(dataID: Long = Matches.map(m => m.dataid).list.distinct.sorted(Ordering[Long].reverse).head) = {
    val outFile = new java.io.File("out.csv")
    val writer = CSVWriter.open(outFile)

    val filteredData = Matches.filter(m => m.dataid === dataID).list.map(m => 
    List(m.docname, 
        withHyperlink("showOriginal/" + m.dataid.toString.dropRight(4),"view original"),          
        withHyperlink("showExtractFoundation/" + m.dataid.toString.dropRight(4) + s"?dataID=${m.dataid}","view result"),
        m.dataid, m.sentence, m.matchpattern, m.locationactual, m.locationtest, m.fullmatch, m.matchindication))
        val data = List("Run ID", "", "", "Article", "Sentence", "Pattern", "Location Test", "Location Actual", "Final Match?", "Match Indication") :: filteredData 
        writer.writeAll(data)
  }
}

object createAnalyticSummary extends Connection with googleSpreadsheetCreator {
  import com.github.tototoshi.csv._ // only good for "small" csv files; https://github.com/tototoshi/scala-csv/issues/11
  def go(dataID: Long = Matches.map(m => m.dataid).list.distinct.sorted(Ordering[Long].reverse).head) = {
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
    
    /*
    val result : List[List[Any]] = grouped.map { case(docName, matches) =>  
                                     List(withHyperlink("showOriginal/" + docName.dropRight(4), docName), hasLimitationSection("ubuntu-2014-11-21T12:06:51.286Z")) ++
                                         matchIndications.map(i => matches.filter(m => m.matchindication == i).length)}.toList

    val headerRow = List(List("Article", "has limitation section?") ++ matchIndications.toList)
    val output = headerRow ++ result
    writer.writeAll(output)
    */
  }
}

