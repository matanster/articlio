package com.articlio.ldb

import com.articlio.logger._
import com.articlio.util.text._
import com.articlio.semantic.AppActorSystem
import com.articlio.LanguageModel._

//
// Initialize ldb from more raw rules, and expose it.
// TODO: return what needs to be exposed rather than expose so many members?...
//
class LDB(inputRules: Seq[RuleInput], logger: SimpleLogger) {
  
  logger.write(inputRules.mkString("\n"), "db-rules1.1")    
  
  //
  // expand base rules into more rules - quite not triggered from the database data right now -
  // wrong approach probably
  //
  def expand(rules: Seq[SimpleRule]) : Seq[ExpandedRule] = {
    
    AppActorSystem.timelog ! "exapanding patterns containing article-self-references into all their combinations"  
    
    val ASRRules : Seq[ExpandedRule] = rules.filter(rule => rule.pattern.containsSlice("{asr")) map ExpandedRule
    println(ASRRules)
    
    for (rule <- ASRRules) { 
      rule.fragmentType match {
      case VerbFragment => for (ref <- SelfishReferences.all if (ref.annotatedL1.isnt(Personal))) 
        println()
        //println(ref.annotatedL1.sequence + rule.rule.pattern)
      case NounFragment => for (ref <- SelfishReferences.all if (ref.annotatedL1.is(PossesivePronoun)))  // must have props PossesivePronoun or possibly nounphrase + 's
        println()
        //println(ref.annotatedL1.sequence + rule.rule.pattern)
      case InByOfFragment => for (ref <- SelfishReferences.all if (ref.annotatedL1.isAnyOf(Set(Personal, Possesive))))
        println(ref.annotatedL1.sequence + rule.rule.pattern)
        
      }
    }
  
  //
  
  //val expansion : Seq[String] = ASRRules.flatMap (rule => ArticleSelfReference.refsText.map
  //                                      (refText => rule.pattern.patch(rule.pattern.indexOfSlice("{asr}"), refText, "{asr}".length)))
  
  //println(ASRRules.mkString("\n"))
  //println(expansion.mkString("\n"))
  println(rules.length)
  println(ASRRules.length)
  //println(expansion.length)
  
  AppActorSystem.timelog ! "exapanding patterns containing article-self-references into all their combinations"  
  return ASRRules
  }
  
  // build the data structures
  private val wildcards = List("..", "…")      // wildcard symbols allowed to the human who codes the CSV database
      private val wildchars = List('.', '…', ' ')  // characters indicating whether we are inside a wildcard sequence.. hence - "wildchars"
      
      // breaks down a wildcard-containing pattern into a list of its fragments 
      def breakDown(pattern: String): List[String] = {
    val indexes = wildcards map pattern.indexOf filter { i: Int => i > -1 } // discard non-founds
    if (indexes.isEmpty) {
      return(List(pattern))
    }
    else {
      val pos = indexes.min
          val (leftSidePlus1, rest) = pattern.splitAt(pos); val leftSide = leftSidePlus1.dropRight(1) // split and drop space
          val rightSide = rest.dropWhile((char) => wildchars.exists((wildchar) => char == wildchar))
          return List(leftSide) ::: breakDown(rightSide)
    }
  }
  
  AppActorSystem.timelog ! "patterns representation building"
  
  //inputRules map (r => println(r.properties.get.filter(property => property.isInstanceOf[LocationProperty])))
  
  val rules: Seq[SimpleRule] = inputRules map (inputRule => 
  new SimpleRule(inputRule.pattern, 
      breakDown(deSentenceCase(inputRule.pattern)), 
      inputRule.indication, 
      // inputRule.properties.collect { case locationProp : LocationProperty => locationProp }))
      if (inputRule.properties.isDefined) 
        inputRule.properties.get.filter(property => property.isInstanceOf[LocationProperty]) match {
        case s: Seq[Property] => if (s.length>0) Some(inputRule.properties.get.filter(property => property.isInstanceOf[LocationProperty])) else None
        case _ => None
      } else None,
      if (inputRule.properties.isDefined) 
        inputRule.properties.get.filter(property => property.isInstanceOf[ReferenceProperty]) match {
        case s: Seq[Property] => if (s.length>0) Some(inputRule.properties.get.filter(property => property.isInstanceOf[ReferenceProperty])) else None
        case _ => None
      } else None))  
      
      logger.write(rules.mkString("\n"), "db-rules2")                                                      
      
      // patterns to indications map - 
      // each pattern correlates to only one indictaion 
      val patterns2indications : Map[String, String] = rules.map(rule => (rule.pattern -> rule.indication)).toMap
      
      // patterns to fragments map - 
      // fragments collections are scala Lists as order and appearing-more-than-once matter
      val patterns2fragments : Map[String, List[String]] = rules.map(rule => (rule.pattern, rule.fragments)).toMap
      
      // fragments to patterns map 
      // (constructed through a mutable builder - to avoid the memory hogging of the alternative pure functional implementation)
      // the pattern collections here are ultimately scala Sets because order and appearing-more-than once are not necessary
      private val builder = new collection.mutable.HashMap[String, collection.mutable.Set[String]] 
          with collection.mutable.MultiMap[String, String]                                  // this is a multimap builder, the way scala needs it
              rules.foreach(rule => rule.fragments.foreach(fragment => builder addBinding (fragment, rule.pattern)))  // build it
              val fragments2patterns : Map[String, Set[String]] = builder.map(kv => kv._1 -> kv._2.toSet).toMap       // extract to immutable
              
              //
              // map back from patterns to rules (needed as aho-corasick returns strings not rules that included them)
              //
              val patterns2rules : Map[String, SimpleRule] = rules.map(rule => rule.pattern -> rule).toMap
              
              // bag of all fragments - 
              // uses a Set to avoid duplicate strings
              val allFragmentsDistinct : Set[String] = rules.map(rule => rule.fragments).flatten.toSet
              
              AppActorSystem.timelog ! "patterns representation building"
              // TODO: uncomment SelfMonitor.logUsage("after patterns representation building is")
              logger.write(allFragmentsDistinct.mkString("\n"), "db-distinct-fragments")
              logger.write(patterns2fragments.mkString("\n"), "db-rule-fragments")
              
              expand(rules) // should do nothing for now
              
}
