package com.articlio.actors;
/*

package com.articlio.semantic;
import org.vertx.scala.core._
import org.vertx.scala.core.http.HttpServerRequest
import org.vertx.scala.core.http.HttpClientResponse
import org.vertx.scala.platform.Verticle
import org.vertx.scala.core.buffer.Buffer
import scala.io.Source
//import java.net.URLEncoder
//import spray.json._
//import DefaultJsonProtocol._
import org.ahocorasick.trie._
import scala.collection.JavaConversions._

/*

import scala.reflect.ClassTag
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.{ILoop, NamedParam}

class ClassLoaderHint {
}

def break(args: NamedParam*) = {
  val repl = new ILoop()

  repl.settings = new Settings()
  repl.settings.embeddedDefaults[ClassLoaderHint]
  repl.settings.Yreplsync.value = true
  repl.in = repl.chooseReader(repl.settings)

  repl.createInterpreter()

  args.foreach(p => repl.bind(p.name, p.tpe, p.value))

  repl.loop()
  repl.closeInterpreter()
}
*/

object Deployer extends Verticle {
  override def start() {
    println("starting deployer verticle")
    container.logger.info("starting deployer verticle")
    container.deployVerticle("scala:com.articlio.ldb.ldb")
    container.deployVerticle("scala:com.articlio.selfMonitor")
  }
}

class ldb extends Verticle {
  override def start() {

    val trie = new Trie
    trie.onlyWholeWords();
    trie.addKeyword("sugar")
    val emits = trie.parseText("sugar cane sugarcane sugar canesugar")
    emits.foreach(println)

    def f[T](v: T) = v
    val t = emits.getClass
    println(t)
  
    //container.logger.info("starting linguistic database verticle")
    System.out.println("starting linguistic database verticle")
    val databaseInputFile = "/home/matan/Downloads/database-machine-ready July 24 2014 non-abstract database - Markers.csv"
    for (row <- Source.fromFile(databaseInputFile).getLines()) {
      val array = row.split(",")
    }
  }
}
*/