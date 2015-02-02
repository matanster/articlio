//
// for ways to scale this more, see:
//
// 1) https://groups.google.com/forum/#!msg/scalaquery/WZJ9Bm92Yfw/ANP10OY5pQoJ
// 2) http://blog.wikimedia.org/2011/11/21/do-it-yourself-analytics-with-wikipedia/ (wikihadoop et al.)
//

package com.articlio.analyze
import com.articlio.storage.{Connection,Match}
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._
import scala.slick.util.CloseableIterator
import com.articlio.semantic.AppActorSystem

object Indels extends Connection with Match {

  val changeAnalyticsLogger= new com.articlio.util.Logger("global-change-analytics")
  
  val newResults = matches.filter(_.runID === "ubuntu-2014-12-15 21:09:52.072").sortBy(_.sentence).iterator
  val oldResults = matches.filter(_.runID === "ubuntu-2014-12-13 18:37:53.728").sortBy(_.sentence).iterator
  
  val dropped = Seq.newBuilder[Match]
  val added = Seq.newBuilder[Match]
  
  def myCompare(a: Option[Match], b: Option[Match]) : Int = {
    
    if (a.isDefined && !b.isDefined) return -1
    if (!a.isDefined && b.isDefined) return  1
    
    return (a.get._3 compare b.get._3)
  }
  
  def myNext(i: CloseableIterator[Match]) : Option[Match] = {
    i.hasNext match {
      case true  => Some(i.next)
      case false => None
    }
  }
  
  def go (newResult: Option[Match], oldResult: Option[Match]): Unit = {
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

  changeAnalyticsLogger.write(dropped.result.map(_._3).mkString("\n"), "rows-dropped")
  changeAnalyticsLogger.write(added.result.map(_._3).mkString("\n"), "rows-added")
  
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
    
  matches.foreach { 
    case(runID, docName, sentence, matchPattern, locationTest, locationActual, isFinalMatch, matchIndication) =>
  }
}