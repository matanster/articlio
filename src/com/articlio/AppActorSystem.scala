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
  
  import com.articlio.storage.DefaultDB.db
  val appActorSystem = new AppActorSystem
}