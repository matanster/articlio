package com.articlio

import akka.actor.ActorSystem
import akka.actor.Props
import com.articlio.util.Timelog
import com.articlio.storage.OutDB
import com.articlio.storage.SlickDB
import com.articlio.logger._

object Globals {
       
  val system = ActorSystem("app-actor-system")
  
  val timelog = system.actorOf(Props[Timelog], name = "timer-logging-service") // TODO: message passing may skew timing, pluck this out everywhere
                                             
  val deduplicator = system.actorOf(Props(new com.articlio.dataExecution.Deduplicator), name="deduplicator")
  
  // val outDB = system.actorOf(Props(new OutDB), name = "out-DB-service")
  
  //import argonaut._, Argonaut._
  val ownGitVersion = "git " + sys.process.Process(Seq("git", "describe", "--always", "--dirty")).!!.dropRight(1) // http://stackoverflow.com/questions/29527908/get-hash-without-committing/29528854#29528854
  
  def shutdown {                                        
    system.shutdown
  }

  val mode = play.api.Play.current.configuration.getString("mode")

  implicit val db = mode match {
    case Some("real") | None => com.articlio.storage.slickDb 
    case Some("test")        => com.articlio.storage.slickTestDb
    case Some(other)         => throw new Throwable(s"Invalid mode parameter: $other")
  }

  val outDB = OutDB(db) /// no longer an actor, no longer needs to be in an actor system
    
  val dataFilesRoot = mode match {
    case Some("real") | None => "../data/" 
    case Some("test")        => "../test-data/"
    case Some(other)         => throw new Throwable(s"Invalid mode parameter: $other")
  }

  val logger = mode match {
    case Some("real") | None => Logger(VoidTagFilter, DefaultExpander, DefaultUnderlyingExternalLogger)
    case Some("test")        => Logger(VoidTagFilter, DefaultExpander, TestDefaultUnderlyingExternalLogger)
    case Some(other)         => throw new Throwable(s"Invalid mode parameter: $other")  
  }

  println("Initialized")
}