/*
 *  Obsolete test out of slick - although nice idea to have a separate db for pdf conversion data, for safe purging of each one 
 */

/*
//
// Experiment with analyzing header detection data created by the node.js app 
//

package com.articlio.storage

//import language.experimental.macros
import slick.driver.MySQLDriver.api._
//import scala.slick.lifted._ // more clumsy to debug/use than "direct/simple"
//import slick.direct._   // http://slick.typesafe.com/doc/2.1.0/direct-embedding.html

// direct SQL access imports
//import scala.slick.driver.JdbcDriver.backend.Database
//import Database.dynamicSession
//import scala.slick.jdbc.{GetResult, StaticQuery}

object InDB {

  // connection parameters
  val host     = "localhost"
  val port     = "3306"
  val database = "articlio"
  val user     = "articlio"
  
  // acquire single database connection used as implicit throughout this object
  println("starting input DB connection...")
  val db = Database.forURL(s"jdbc:mysql://$host:$port/$database", user, driver = "com.mysql.jdbc.Driver")
  implicit val session: Session = db.createSession

  type Header = (Int, Int, String, String, String, String)

  // slick class binding definition
  class Headers(tag: Tag) extends Table[Header](tag, "headers") {
    def level = column[Int]("level")
    def tokenId  = column[Int]("tokenId")
    def header  = column[String]("header")
    def detectionComment  = column[String]("detectionComment")
    def docName = column[String]("docName")
    def runId = column[String]("runId")
    def * = (level, tokenId, header, detectionComment, docName, runId)
  }

  // the table handle
  val headers = TableQuery[Headers]

  def close = session.close
  
  def Test(runIdFilter: String) {
    headers foreach { case (level, tokenId, header, detectionComment, docName, runId) => 
        println(runId)
      }
      
  }
  def Query(runIdFilter: String) {
     val filtered = headers.filter(row => row.runId === runIdFilter)
     val grouped = filtered.groupBy(row => row.docName).map( { case (docName, group) => (docName, group.length) } ).list
     //val groupes = filtered.groupBy(row => row.docName).map( { case (docName, group) =>  (docName, group.length, group.map(groupRow => groupRow.header).filter(a => a === "intro").length) } ).list
     val groupes = filtered.groupBy(row => row.docName).map( { case (docName, group) =>  (docName, group.length, group.filter(groupRow => groupRow.header === "Introduction").length) } ).list
  }  
}
*/