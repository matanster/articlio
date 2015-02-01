package com.articlio.semantic

import akka.actor.ActorSystem
import com.articlio.config
import com.articlio.SelfMonitor
import com.articlio.util.{Console}
import com.articlio.storage
import com.articlio.analyze._
import com.articlio.ldb

object Boot extends App {

  SelfMonitor

  //
  // move to http-invoked if not already there
  //
  // val i = Indels
  
  AppActorSystem.outDB ! "createIfNeeded"
  
  //ldb.ldb.init

  //com.articlio.storage.createCSV.go()
  //com.articlio.storage.createAnalyticSummary.go()
  
  val httpService = HttpService
                                              
  // Ultimately something like http://stackoverflow.com/questions/24731242/spray-can-webservice-graceful-shutdown
  // should be used for the following lines of code, as much as skipping them has no ecosystemic side effects:
  // Monitor.shutdown
  // storage.OutDB.close
}

object Analytic {
  val inDb = storage.InDB.Query("ubuntu-2014-11-21T12:06:51.286Z")
  storage.InDB.close
} 