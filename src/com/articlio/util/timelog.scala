package com.articlio.util
import akka.actor.Actor
import com.articlio.Globals.appActorSystem
import com.articlio.logger._


//
// Simple facility for timing code - timings get data logged, and written to the console
//
class Timelog extends Actor {

  val logger = new SimplestLogger("global-timers")
  val timers = scala.collection.mutable.Map.empty[String, Long] // use new instead of empty then with scala.collection.concurrent.Map[String, Long] doesn't work

  //
  // Usage: call once to start the timer, and once to stop it, using the same timer name parameter
  //
  private def timer(timerName:String) = {
    if (timers contains timerName) {
      val output = s"$timerName took ${(System.nanoTime() - timers(timerName)) / 1000 / 1000} milliseconds"
      timers -= timerName
      logger.write(output, Some(ConsoleMirror))
    }
    else timers(timerName) = System.nanoTime()
  }

  //
  // Usage: wraps around a function (or code block)
  //
  private def time[T](func: => T): T = {
    val start = System.nanoTime()
    val result = func // invoke the wrapped function
    val output = s"function took ${(System.nanoTime() - start) / 1000 / 1000} milliseconds"
    logger.write(output, Some(ConsoleMirror))
    return(result)
  }
  
  def receive = { 
    case timerName: String => timer(timerName)
    case _ => throw new Exception("unexpected actor message type received")
  }
}