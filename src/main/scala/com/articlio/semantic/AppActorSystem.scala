package com.articlio.semantic
import akka.actor.ActorSystem
import akka.actor.Props
import com.articlio.util.Timelog
import com.articlio.storage.OutDB

object AppActorSystem {
  val system = ActorSystem("app-actor-system")
  val timelog = AppActorSystem.system.actorOf(Props[Timelog], 
                                              name = "timer-logging-service")
  val outDB = AppActorSystem.system.actorOf(Props[OutDB], 
      name = "out-DB-service")
                                                                                          
}