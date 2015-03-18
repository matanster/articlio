//
// for ways to scale this more, see:
//
// 1) https://groups.google.com/forum/#!msg/scalaquery/WZJ9Bm92Yfw/ANP10OY5pQoJ
// 2) http://blog.wikimedia.org/2011/11/21/do-it-yourself-analytics-with-wikipedia/ (wikihadoop et al.)
//

package com.articlio.analyze
import com.articlio.storage.{Connection}
import slick.driver.MySQLDriver.api._
import slick.jdbc.meta._
import com.articlio.storage.slickDb._
import slick.util.CloseableIterator
import com.articlio.AppActorSystem
import models.Tables._
import com.articlio.logger.SimpleLogger
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/*
 *  TODO: parameterize and connect from http routes..
 *  TODO: turn this to use slick streams and be non-blocking, rather than block for using the old slick iterators,
 *        see http://slick.typesafe.com/doc/3.0.0-RC1/database.html#streaming for streams api adaptation.
 */

object Indels extends Connection {

  val changeAnalyticsLogger= new SimpleLogger("global-change-analytics")
  
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  val newResults = Await.result(dbQuery(Matches.filter(_.dataid === 111L).sortBy(_.sentence)), Duration.Inf).iterator
  val oldResults = Await.result(dbQuery(Matches.filter(_.dataid === 222L)), Duration.Inf).iterator
  
  val dropped = Seq.newBuilder[MatchesRow]
  val added = Seq.newBuilder[MatchesRow]
  
  def myCompare(a: Option[MatchesRow], b: Option[MatchesRow]) : Int = {
    
    if (a.isDefined && !b.isDefined) return -1
    if (!a.isDefined && b.isDefined) return  1
    
    return (a.get.sentence compare b.get.sentence)
  }
  
  def myNext(i: Iterator[MatchesRow]) : Option[MatchesRow] = {
    i.hasNext match {
      case true  => Some(i.next)
      case false => None
    }
  }
  
  def go (newResult: Option[MatchesRow], oldResult: Option[MatchesRow]): Unit = {
    if (!newResult.isDefined && !oldResult.isDefined) return
    
    myCompare(newResult, oldResult) match {
      case x if x == 0 => 
        go(myNext(newResults), myNext(oldResults))
      case x if x > 0 => 
        dropped += oldResult.get
        go(newResult, myNext(oldResults))
      case x if x < 0 => 
        added += newResult.get
        go(myNext(newResults), oldResult)
    }
  }
  
  AppActorSystem.timelog ! "analyzing"
  go(myNext(newResults), myNext(oldResults))
  AppActorSystem.timelog ! "analyzing"
  
  println(dropped.result.length)
  println(added.result.length)

  changeAnalyticsLogger.write(dropped.result.map(_.sentence).mkString("\n"), "rows-dropped")
  changeAnalyticsLogger.write(added.result.map(_.sentence).mkString("\n"), "rows-added")
  
  //println(added.result.mkString("\n"))
  
  /*
  val chunkSize = 1000
  val run1Grouped = run1.grouped(chunkSize)
  val run2Grouped = run2.grouped(chunkSize)
  */
  
  /*
  val indels = for {
    r1 <- run1
    r2 <- run2 if r2.sentence === r1.sentence 
  } yield r1.sentence
  */
    
  //Matches.foreach { 
  //  case(runID, docName, sentence, matchPattern, locationTest, locationActual, isFinalMatch, matchIndication) =>
  //}
}

/*
 * slick streaming version of the above - frozen till it proves better than above
 */
/*
object IndelsStreaming extends Connection {

  val changeAnalyticsLogger= new SimpleLogger("global-change-analytics")
  
  implicit val context = play.api.libs.concurrent.Execution.Implicits.defaultContext
  val newResults = db.stream(Matches.filter(_.dataid === 111L).sortBy(_.sentence).result)
  val oldResults = db.stream(Matches.filter(_.dataid === 222L).result)
  
  newResults.
  
  val dropped = Seq.newBuilder[MatchesRow]
  val added = Seq.newBuilder[MatchesRow]
  
  def myCompare(a: Option[MatchesRow], b: Option[MatchesRow]) : Int = {
    
    if (a.isDefined && !b.isDefined) return -1
    if (!a.isDefined && b.isDefined) return  1
    
    return (a.get.sentence compare b.get.sentence)
  }
  
  def myNext(i: Iterator[MatchesRow]) : Option[MatchesRow] = {
    i.hasNext match {
      case true  => Some(i.next)
      case false => None
    }
  }
  
  def go (newResult: Option[MatchesRow], oldResult: Option[MatchesRow]): Unit = {
    if (!newResult.isDefined && !oldResult.isDefined) return
    
    myCompare(newResult, oldResult) match {
      case x if x == 0 => 
        go(myNext(newResults), myNext(oldResults))
      case x if x > 0 => 
        dropped += oldResult.get
        go(newResult, myNext(oldResults))
      case x if x < 0 => 
        added += newResult.get
        go(myNext(newResults), oldResult)
    }
  }
  
  AppActorSystem.timelog ! "analyzing"
  go(myNext(newResults), myNext(oldResults))
  AppActorSystem.timelog ! "analyzing"
  
  println(dropped.result.length)
  println(added.result.length)

  changeAnalyticsLogger.write(dropped.result.map(_.sentence).mkString("\n"), "rows-dropped")
  changeAnalyticsLogger.write(added.result.map(_.sentence).mkString("\n"), "rows-added")
}*/