package com.articlio.ldb
import org.ahocorasick.trie._
import com.articlio.util._
import com.articlio.util.text._
import scala.collection.JavaConverters._
import com.articlio.semantic.AppActorSystem
import akka.actor.Actor
import akka.event.Logging

//
// Aho-Corasick trie (for searching all pattern fragments implied in given linguistic database)
//
class AhoCorasickTrie {

  val trie = new Trie

  def init (fragments: Set[String]) {
    AppActorSystem.timelog ! "aho-corasick initialization (lazy operations not necessarily included)"
    trie.onlyWholeWords()
    fragments foreach trie.addKeyword
    AppActorSystem.timelog ! "aho-corasick initialization (lazy operations not necessarily included)"
  }

  //
  // invoke aho-corasick to find all fragments in given sentence
  //
  def findAll(sentence : String, logger: Logger) : List[Map[String, String]] = 
  {
    //println(deSentenceCase(sentence))
    val emitsJ = trie.parseText(deSentenceCase(sentence))

    if (emitsJ.size > 0) {
      val emits = (emitsJ.asScala map (i => Map("start" -> i.getStart.toString, "end" -> i.getEnd.toString, "match" -> i.getKeyword.toString))).toList
      logger.write(sentence, "sentence-fragment-matches")
      logger.write(emits.mkString("\n") + "\n", "sentence-fragment-matches")
      return(emits)
    }
    else return (List.empty[Map[String, String]])
  }
}

//
// Actor wrapper
//
class AhoCorasickActor(ldb: LDB) extends Actor {
  val log = Logging(context.system, this)
  
  val ahoCorasick = new AhoCorasickTrie
  ahoCorasick.init(ldb.allFragmentsDistinct)
  
  def receive = { 
    case ProcessSentenceMessage(s, l) =>
      //log.info(s"received message with sentence: $s")
      sender ! ahoCorasick.findAll(s, l)
    case _ => throw new Exception("unexpected actor message type received")
  }
}

//
// Actor's message type
//
case class ProcessSentenceMessage(sentence : String, logger: Logger)
