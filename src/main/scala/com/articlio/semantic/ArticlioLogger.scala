//
// Might be useful later
//

package com.articlio.semantic

import akka.event.Logging.InitializeLogger
import akka.event.Logging.LoggerInitialized
import akka.event.Logging.Error
import akka.event.Logging.Warning
import akka.event.Logging.Info
import akka.event.Logging.Debug
import akka.actor.Actor

case class Log(message: String)

class ArticlioLogger extends Actor with akka.actor.ActorLogging {
  def receive = {
    case Log(message) => log.info(message)
  }
}