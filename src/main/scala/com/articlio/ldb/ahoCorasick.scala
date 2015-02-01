package com.articlio.ldb
import org.ahocorasick.trie._
import com.articlio.util._
import com.articlio.util.text._
import scala.collection.JavaConverters._    // convert Java colllections to Scala ones
import com.articlio.semantic.AppActorSystem

//
// initalizes an aho-corasick tree for searching all pattern fragments implied in the linguistic database
//
class AhoCorasick {

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
  def go(sentence : String, logger: Logger) : List[Map[String, String]] = 
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