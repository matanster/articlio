package com.articlio.dataExecution

import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import scala.concurrent.Future
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import scala.util.{Success,Failure}
import com.articlio.test.{TestSpec, TestContainer, Testable, Skip, Only}

import com.articlio.dataExecution.concrete._

/*
 * 
 * Actor class that makes sure that a "same" data creation attempt is only handled once, then notified of to all awaiting callers.
 * Why actor for it? an actor processes messages one-by-one, so it can safely avoid any race condition 
 * 
 */
class Deduplicator extends Actor with DataExecution {
  
  val log = Logging(context.system, this)
  
  // this is the only concrete implementation available out of the box in scala 2.11, for a thread-safe map
  // (refer to http://stackoverflow.com/questions/18660769/best-practices-for-mixing-in-scala-concurrent-map)
  val started : scala.collection.concurrent.Map[String, Future[ExecutedData]] = 
    scala.collection.concurrent.TrieMap.empty[String, Future[ExecutedData]] 
  
  def hash(data: DataObject) = data.dataType + data.dataTopic 
  
  def receive = {
    case Get(data) => {
      val dataHash = hash(data)  
      started.get(dataHash) match {
        case Some(future) => println(Console.BLUE_B + "info: multiple requests waiting on single data creation" + Console.RESET); sender ! future
        case None   => {
          println(Console.RED_B + s"none yet started for $data.dataType" + Console.RESET)
          val future = attemptCreate(data)
          started + ((dataHash, future))            
          sender ! future
          future.onComplete { _ => started.remove(dataHash) }
        }
      }
    }
  }
}

// actor message type
final case class Get(data: DataObject)

object Deduplicator extends Testable { // companion object for tests 
  object TestContainer extends TestContainer {
    import com.articlio.Globals.db
    import models.Tables._
    import slick.driver.MySQLDriver.api._
    
    def tests = Seq(
        new TestSpec(given  = "a request for data that is already in process of creation", 
                     should = "only create data once",
                     identicalDataDeduplicate)
    )
  
    def allApplicableDataIDs(dataType: String, dataTopic: String) = // TODO: collapse and relocate with same function in ShowExtract.scala
      db.query(Data.filter(_.datatype === dataType)
                   .filter(_.datatopic === dataTopic)
                   .map(_.dataid))
  
    def identicalDataDeduplicate = {
      
      val testDataTopic = "deduplicator-dummy-test-data"
      
      val slowData = DummyWithDuration(testDataTopic, 1000)
      
      val future1 = FinalDataNew(slowData) // request a test data once 
      val future2 = FinalDataNew(slowData) // request it again while first request is surely still in progress
      
      Future.sequence(Seq(future1, future2)) flatMap {_ => 
        allApplicableDataIDs(DummyWithDuration.getClass.getSimpleName.filter(_ != '$'), testDataTopic) map { 
          dataIDs => (dataIDs.length == 1) match {
            case true  => // this returns a unit
            case false => throw new Exception(s"should have created one data database entry, but created ${dataIDs.length}")
          }
        }
      } 
    }
  }  
}