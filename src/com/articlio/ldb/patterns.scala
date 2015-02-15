package com.articlio.ldb

import com.articlio.input._
import com.articlio.util._
import com.articlio.util.text._
import com.articlio.LanguageModel._
import com.articlio.SelfMonitor
import com.articlio.semantic.AppActorSystem
//import com.articlio.storage.Match
import scala.collection.JavaConverters._
import scala.concurrent.Future
import scala.concurrent.Await
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.actor._
import akka.actor
import akka.routing.BalancingPool
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import models.Tables._
import models.Tables.{Data => DataRecord}
import com.articlio.storage.Connection
import scala.slick.driver.MySQLDriver.simple._
import scala.slick.jdbc.meta._

abstract class PlugType
case object    RefAppendable  extends PlugType // self reference is potentially *appendable* to target phrase
case object    RefPrependable extends PlugType // self reference is potentially *prependable* to target phrase

abstract class PotentiallyPluggable (val plugType: PlugType)
//case class AA extends PotentiallyPluggable(RefPrependable)
case object    VerbFragment extends PotentiallyPluggable(RefPrependable)
case object    NounFragment extends PotentiallyPluggable(RefPrependable)
case object    InByOfFragment   extends PotentiallyPluggable(RefAppendable)

//
// class hierarchy for describing rules as derived by the LDB object
//

abstract class Rule
case class SimpleRule (pattern: String, 
                       fragments: List[String], 
                       indication: String, 
                       locationProperty: Option[Seq[Property]], 
                       ReferenceProperty: Option[Seq[Property]]) 

case class ExpandedRule (rule: SimpleRule) extends Rule {
  def getFragmentType : PotentiallyPluggable = {
                          if (rule.pattern.containsSlice("{asr-V}")) return VerbFragment 
                          if (rule.pattern.containsSlice("{asr-N}")) return NounFragment
                          return InByOfFragment
  }

  val fragmentType = getFragmentType
  override def toString = s"$rule, $fragmentType"
}


case class ldb(csvFile: String) extends Connection {

  val globalLogger = new Logger("global-ldb")
  val overallLogger = new Logger("overall")
  
  //
  // Builds and exposes the data structures necessary for working the rules database
  // Refactor opportunity: use newBuilder and .result rather than hold mutable and immutable collections - for correct coding style without loss of performance!
  //
  class LDB(inputRules: Seq[RuleInput]) {
  
    globalLogger.write(inputRules.mkString("\n"), "db-rules1.1")    
    
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
                       
    globalLogger.write(rules.mkString("\n"), "db-rules2")                                                      
    
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
    globalLogger.write(allFragmentsDistinct.mkString("\n"), "db-distinct-fragments")
    globalLogger.write(patterns2fragments.mkString("\n"), "db-rule-fragments")

    expand(rules) // should do nothing for now

  }

  //
  // match rules per sentence    
  //
  val inputRules = CSV.deriveFromCSV(csvFile)
  val db = new LDB(inputRules)
    
  case class Go(sentence : String, logger: Logger)
  
  class AhoCorasickActor extends Actor {
    val log = Logging(context.system, this)
    
    val ahoCorasick = new AhoCorasick
    ahoCorasick.init(db.allFragmentsDistinct)
    
    def receive = { 
      case Go(s, l) =>
        //log.info(s"received message with sentence: $s")
        sender ! ahoCorasick.go(s, l)
      case _ => throw new Exception("unexpected actor message type received")
    }
  }

  val concurrency = 4
  
  val ahoCorasick = AppActorSystem.system.actorOf(Props[AhoCorasickActor].withRouter(BalancingPool(nrOfInstances = concurrency)), 
                                             name = "aho-corasick-pool-service") 
     
  //val ahoCorasick = new Array[AhoCorasickActor](concurrency)
                                                                       
  val SPACE = " "
                                   
  def goWrapper(articleName: String, document: JATS) : Boolean = {
    
    def localNow = new java.sql.Timestamp(java.util.Calendar.getInstance.getTime.getTime) // this follows from http://alvinalexander.com/java/java-timestamp-example-current-time-now
                                                                                      // TODO: need to switch to UTC time for production
    val ownHostName = java.net.InetAddress.getLocalHost.getHostName
    
    val startTime = localNow
     
    val dataid = DataRecord.returning(DataRecord.map(_.dataid)) += DataRow(
      dataid                 = 0L, // will be auto-generated 
      datatype               = "semantic", 
      datatopic              = articleName, 
      creationstatus         = "started", 
      creationerrordetail    = None,
      creatorserver          = ownHostName,
      creatorserverstarttime = Some(startTime),
      creatorserverendtime   = None,
      datadependedon         = None)

    
    val success = go(dataid, articleName, document) match {
      case None  => "successfully created"
      case Some(error) => "not successfully created"
    }
    
    DataRecord.filter(_.dataid === dataid).update(DataRow( // alternative way for only modifying select fields at http://stackoverflow.com/questions/23994003/updating-db-row-scala-slick
      dataid                 = dataid,
      datatype               = "semantic", 
      datatopic              = articleName, 
      creationstatus         = "started", 
      creationerrordetail    = None,
      creatorserver          = ownHostName,
      creatorserverstarttime = Some(startTime),
      creatorserverendtime   = Some(localNow),
      datadependedon         = None)
    ) 
        
    true 
  }
  
  import com.articlio.dataExecution.CreateError 
  def go (runID: Long, articleName: String, document: JATS) : Option[CreateError] = {

    val logger = new Logger(document.name)
    
    //
    // get data
    //
    val sections : Seq[JATSsection] = document.sections // elife04395, elife-articles(XML)/elife00425styled.xml
    //if (sections.isEmpty) return s"${document.name} appears to have no sections and was not processed"  
    if (sections.isEmpty) return Some(CreateError("no sections detected in JATS input - cannot extract")) // Seq.empty[Matches]  
    
    implicit class MyStringOps(val s: String) { // TODO: separate into util package object or something 
      def endsWithAny(any: Seq[String]) : Boolean = {
        for (word <- any)
          if (s.endsWith(SPACE + word)) return true
        return false
      }
    }
    
    val specialCaseWords = Seq(" vs.", " al.", " cf.", " st." ," Fig.", " FIG.", "pp.")
    
    def sentenceSplitRecursive (text: String) : Seq[String] = {

      if (text.isEmpty) 
        return Seq.empty[String]

      for (i <- 2 to text.length) {

        val tentative = text.take(i) 

        if (tentative.endsWith(". "))      
          if (tentative.dropRight(1).endsWithAny(specialCaseWords) && text.isDefinedAt(i) && (text.charAt(i).isUpper))
            return Seq(tentative) ++ sentenceSplitRecursive(text.drop(i))

        if (tentative.endsWith(". "))      
            return Seq(tentative.dropRight(1)) ++ sentenceSplitRecursive(text.drop(i))

        if (tentative.endsWith("? ") || tentative.endsWith("! "))      
            return Seq(tentative.dropRight(1)) ++ sentenceSplitRecursive(text.drop(i))
      }

      return Seq(text) // getting to the end of the text without sentence delimitation having been detected, 
                       // this code flushes the entire text as one sentence.
                       // future intricacies may call to trigger a notice here.
    }
  
   
   //   
   // splits a text into sentences - non recursive version for scalability
   // Note: removes the typical space trailing a sentence ending.
   // 
   def sentenceSplit (text: String) : Seq[String] = {
      def isWordSeparator(c: Character) : Boolean = Set(' ', '\n', '(').contains(c) // move to util       
      import scala.math.{min, max}
      var sentences = Seq.newBuilder[String] 
      
      var i = 0
      var j = 0 // used to skip over disqualified stop sequence
      
      // scan the text, gulping a sentence whenever one is identified
      while (i<text.length) { 
        val remaining = text drop i  // the text remaining after previous sentence gulps
        val find = Seq(remaining.indexOfSlice(". ", j), remaining.indexOfSlice("! ", j),  remaining.indexOfSlice("? ", j)).filter(_ != -1)
        if (find.isEmpty) { sentences += remaining; i = text.length}
        else {
          val t = find.reduceLeft(min) // (start of) first tentative stop sequence encountered 
          val tentative = remaining.take(t+1) // take up until and including the period/exclamation/question mark
          if (tentative.endsWithAny(specialCaseWords) || tentative.charAt(max(tentative.length-3,0)) == '.') {
            val afterSpace = t+1+2 
            if (afterSpace < text.length && (text.charAt(afterSpace).isUpper)) { sentences += tentative; i += t + 2; j = 0} else j = t + 1
          } 
          else if (isWordSeparator(tentative.charAt(max(tentative.length - 3, 0)))) j = t + 1 // the case of single letter name initial (e.g. "C. ")
          else { sentences += tentative; i += t + 2; j = 0}
        }
      }
      return sentences.result
    }

    case class AnnotatedSentence(text : AnnotatedText, section: String)

    case class LocatedText(text: String, section: String)

    def sentenceSplitter (toSplit: LocatedText) : Seq[LocatedText] = {

      logger.write(toSplit.text, "JATS-paragraphs")
      //println(toSplit.text)
      val sentences = sentenceSplit(toSplit.text)
      logger.write(sentences.mkString("\n"), "JATS-sentences")
      return sentences map (sentence => LocatedText(sentence, toSplit.section))
    }

    // flat map all section -> paragraph -> sentences into one big pile of sentences. 
    val sentences: Seq[LocatedText] = sections.flatMap(section =>  section.paragraphs.flatMap(p =>
       sentenceSplitter(LocatedText(p.sentences.map(s => s.text).mkString(""),  section.sectionType)))) 

    println(s"number of sentences: ${sentences.length}")
       
    val sectionTypeScheme = document.sectioningType match {
     case "pdf-converted" => pdfConvertedSectionTypeScheme
     case _ => eLifeSectionTypeScheme 
    }

    //
    // matches rules per sentence    
    //
    def processSentences (sentences : Seq[LocatedText]) = {
  
      val sentenceMatchCount = scala.collection.mutable.ArrayBuffer.empty[Integer] 
      var rdbmsData = Seq.newBuilder[MatchesRow]
      
      implicit val timeout = Timeout(60.seconds)
      
      val results = for (sentenceIdx <- 0 to sentences.length-1) yield {
        val sentence = sentences(sentenceIdx)
        val matchedFragmentsFuture = ask(ahoCorasick, Go(sentence.text, logger)).mapTo[List[Map[String, String]]]
        val matchedFragments = Await.result(matchedFragmentsFuture, timeout.duration)
        sentenceMatchCount += matchedFragments.length

        // checks if all fragments making up a pattern, are contained in target string *in order*
        def isInOrder(fragments: List[String], loc: Integer) : Boolean = {
          if (fragments.isEmpty) return true  // getting to the end of the list without returning false --> true
          else {
            val head = fragments.head

            val matches = matchedFragments.filter(_("match") == head) 
            if (matches.isEmpty) return false                         // has this pattern been matched for this sentence?
            if (matches.exists(_("start").toInt > loc))               // has it been matched in order?
              isInOrder(fragments.tail, (matches.map(_("end").toInt).min))
            else 
              false
          }
        }

        // for each matched fragment, trace back to the patterns to which it belongs,
        // then check if that pattern is matched in its entirety - i.e. if all its fragments match in order.

        val possiblePatternMatches = Set.newBuilder[String] // a Set to avoid duplicates

        matchedFragments.foreach(matched => { 
          val fragmentPatterns = db.fragments2patterns.get(matched("match").toString).get
          possiblePatternMatches ++= fragmentPatterns
        })

        // 
        // decides what to extract. for now extracts the entire sentence,
        // or the entire sentence plus the next one up (in case of special type of cataphora).
        //
        def extraction(sentence: LocatedText) : LocatedText = {
          if (sentence.text.endsWithAny(Seq(":", "the following.", "as follows."))) {
            val resultSpan = sentence.text + " " + sentences(sentenceIdx+1).text // TODO: assure not out of bounds
            overallLogger.write(document.name + ": "  + resultSpan + "\n", "overall-salient-matches")
            return LocatedText(resultSpan, sentence.section)
          }
          else 
            return sentence
        }
        
        val possibleMatches = for (pat <- possiblePatternMatches.result 
                                if (isInOrder (db.patterns2fragments.get(pat).get, -1))) 
                                  yield ( new { var pattern: String = pat;
                                                var locatedText: LocatedText = extraction(sentence);
                                                var indication: String = db.patterns2indications.get(pat).get;
                                                var simpleRule: SimpleRule =db.patterns2rules(pat)})

        possibleMatches.foreach(p =>
          logger.write(Seq(s"sentence '${p.locatedText.text}'",
                           s"in section ${p.locatedText.section}",
                           s"matches pattern '${p.pattern}'",
                           s"which indicates '${p.indication}'").mkString("\n") + "\n","sentence-pattern-matches (location agnostic)"))

        //if (!possibleMatches.isEmpty)logger.write(sentence.text, "output (location agnostic)")
                          
        //
        // checks whether a potential match satisfies its location criteria if any
        // 
        
        def locationTest(p: (String, LocatedText, String, SimpleRule)) : Boolean = {
            var isFinalMatch = false
                if (!p._4.locationProperty.isDefined) {
                  isFinalMatch = true
                }
                else if (p._4.locationProperty.get.head.asInstanceOf[LocationProperty].parameters.exists(parameter =>   // 'using .head' assumes at most one LocationProperty per rule
                sectionTypeScheme.translation.contains(parameter.toLowerCase) && sectionTypeScheme.translation(parameter.toLowerCase) == p._2.section.toLowerCase())) {
                  //println("location criteria matched!")
                  isFinalMatch = true
                }
                else {
                  isFinalMatch = false            
                      if (false)
                      {
                        println
                        println("location criteria not matched for:")
                        println(p._2.text)
                        println("should be in either:")
                        p._4.locationProperty.get.head.asInstanceOf[LocationProperty].parameters.foreach(parameter =>   // 'using .head' assumes at most one LocationProperty per rule
                        if (sectionTypeScheme.translation.contains(parameter.toLowerCase)) println(sectionTypeScheme.translation(parameter.toLowerCase)))
                        println("but found in:")
                        println(p._2.section)
                      }
                }
            return isFinalMatch
        }

        //
        // checks whether a potential match satisfies a selfish reference requirement if any.
        // Note: currently does not distinguish between types of selfish reference at all.
        // 
        def containsSelfRef(text: String) : Boolean = {
          val normalized = deSentenceCase(text)
          println(SelfishReferences.allTexts.filter(s => normalized.indexOf(s) >=0)) 
          SelfishReferences.allTexts.exists(s => normalized.indexOf(s) >= 0) 
        }
        
        def selfishRefTest(p: (String, LocatedText, String, SimpleRule)) : Boolean = {
          var isFinalMatch = false
        	
          if (!p._4.ReferenceProperty.isDefined) isFinalMatch = true         
        	else isFinalMatch = containsSelfRef(p._4.pattern) match {
        	  case true => true // if rule's pattern already contains a selfish reference itself, this is a match
        	  case false => containsSelfRef(p._2.text) 
        	}
          
        	return isFinalMatch
        }
        
	      val tentativeMatches = 
	        possibleMatches.map(p => new { val pattern = p.pattern; 
                                         val locatedText = p.locatedText;
                                         val indication = p.indication;
                                         val simpleRule = p.simpleRule;
                                         val matchesLocation = locationTest(p.pattern , p.locatedText, p.indication, p.simpleRule);
                                         val selfishRef = selfishRefTest(p.pattern, p.locatedText, p.indication, p.simpleRule) }).toSeq
     	
        tentativeMatches.foreach(m => if (!m.selfishRef) println(s"sentence {${m.locatedText.text}} matching pattern {${m.simpleRule.pattern}} does not contain selfish reference and therefore not matched."))

        val finalMatches = tentativeMatches.filter(m => m.matchesLocation && m.selfishRef)
        
        if (!finalMatches.isEmpty) {
          //logger.write(sentence.text, "output")
          logger.write(finalMatches.map(_.locatedText.text).distinct.mkString("\n"), ("output"))  
          
	      finalMatches.foreach(m =>
	        logger.write(Seq(s"sentence '${m.locatedText.text}'",
	                         s"in section ${m.locatedText.section}",
	                         s"matches pattern '${m.pattern}'",
	                         s"which indicates '${m.indication}'").mkString("\n") + "\n","sentence-pattern-matches"))
        }
          
      	rdbmsData ++= 
          tentativeMatches.filter(_.selfishRef).map(m => (runID,
                            document.name, 
                            m.locatedText.text, 
                  				  m.pattern,
                  				  m.simpleRule.locationProperty.isEmpty match {
                  				  	case true  => "any"
                  				  	case false => m.simpleRule.locationProperty.get.head.asInstanceOf[LocationProperty].parameters.mkString(" | ")
                  				  },
                  				  m.locatedText.section,
                  				  m.matchesLocation,
                  				  m.indication).asInstanceOf[MatchesRow])
        //println(rdbmsData)
	           
        //val LocationFiltered = possiblePatternMatches.result.filter(patternMatched => patternMatched.locationProperty.isDefined)

      }
      
      new Descriptive(sentenceMatchCount, "Fragments match count per sentence").all

      AppActorSystem.outDB ! rdbmsData.result
      rdbmsData.result    
    }    
    //return s"Done processing ${document.name}"
    
    //AppActorSystem.timelog ! "matching"
    //AppActorSystem.timelog ! "matching"
    processSentences(sentences)
    
    None // if we got here - return no error    
  }                                                                             
                                                                             
}
