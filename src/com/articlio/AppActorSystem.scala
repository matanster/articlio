package com.articlio

import akka.actor.ActorSystem
import akka.actor.Props
import com.articlio.util.Timelog
import com.articlio.storage.OutDB
import com.articlio.storage.SlickDB

/*
 * an actor system, and some specific actors
 */

object Globals {
  class AppActorSystem(implicit val db: SlickDB) {
    
    val system = ActorSystem("app-actor-system")
    
    val timelog = system.actorOf(Props[Timelog], name = "timer-logging-service") // TODO: message passing may skew timing, pluck this out everywhere
                                               
    val outDB = system.actorOf(Props(new OutDB), name = "out-DB-service")
    
    def shutdown {                                        
      system.shutdown
    }
  }
  
  val mode = play.api.Play.current.configuration.getString("mode")

  implicit val db = mode match {
    case Some("real") | None => com.articlio.storage.slickDb 
    case Some("test")        => com.articlio.storage.slickTestDb
    case Some(other)         => throw new Throwable("Invalid mode parameter: $other")
  }
  
  implicit val dataFilesRoot = mode match {
    case Some("real") | None => "../data/" 
    case Some("test")        => "../test-data/"
    case Some(other)         => throw new Throwable("Invalid mode parameter: $other")
  }

  val appActorSystem = new AppActorSystem
  
  println("Initialized")
}