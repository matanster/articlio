package com.articlio.ldb

import com.articlio.input._
import com.articlio.util._
import com.articlio.util.text._
import com.articlio.LanguageModel._
import com.articlio.SelfMonitor
import com.articlio.semantic.AppActorSystem
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
import com.articlio.dataExecution.CreateError 
import com.articlio.logger._

/*
 *  provide caching & pooling of the heavy initialization of aho-corasick tries 
 */
case class InitializedSeed(router: ActorRef, ldb: LDB)
object ldbEnginePooling {

  val logger = new SimpleLogger("ldb-engine-pooler") // ...might get fancy with a logger per pool later

  // map to a pair of objects - an actor encompassing a trie, 
  // and a compiled ldb. Because callers will currently need both 
  val ldbActorPools = collection.mutable.Map.empty[String, InitializedSeed]
  
  val concurrencyPerActorPool = 10

  private def initialize(inputCSVfileName: String) = {

    println(s"initializing objects for $inputCSVfileName")
    
    //
    // initialize ldb
    //
    val inputRules = CSV.deriveFromCSV(inputCSVfileName)
    val ldb = new LDB(inputRules, logger)
    
    //
    // Start actors ensemble, actors being initialized with it
    //
    val ahoCorasickPool = AppActorSystem.system.actorOf(AhoCorasickActor.props(ldb)
                                    .withRouter(BalancingPool(nrOfInstances = concurrencyPerActorPool)), 
                                     name = s"aho-corasick-tree-pool-${inputCSVfileName.replace(" ","-")}")
                                     // val ahoCorasick = new Array[AhoCorasickActor](concurrency)
                                     
    ldbActorPools += ((inputCSVfileName, InitializedSeed(ahoCorasickPool, ldb)))
  }
  
  def apply(inputCSVfileName: String) : InitializedSeed = {
    if (ldbActorPools.get(inputCSVfileName).isEmpty) initialize(inputCSVfileName) // initialize if necessary 
    ldbActorPools.get(inputCSVfileName).get                                       // provide
  }
  
  // since no control over the number of actor pools is exerted now, 
  // providing API to clear the entire cache makes sense... 
  def clearCache {
    // as per http://doc.akka.io/docs/akka/snapshot/scala/routing.html under #PoisonPill Messages#,
    // explaining how it needs to be used to gracefully shut down (drain) a router and all its children. 
    // Alternatively see https://groups.google.com/forum/#!topic/akka-user/J08T3OyOyMQ 
    ldbActorPools.map { case(_, InitializedSeed(router, _)) => router ! akka.routing.Broadcast(PoisonPill) } 
  }
}

/*
 *  Operates the engine 
 */
case class ldbEngine(inputCSVfileName: String) extends Connection {
  // old tentative TODO: 
  // use newBuilder and .result rather than hold mutable and immutable collections - for correct coding style without loss of performance!

  val logger = new SimpleLogger("ldb-engine")
  //val overallLogger = new Logger("overall")
  
  //val (ahoCorasick, ldb) = ldbEnginePooling(inputCSVfileName) match { case InitializedSeed(actorRef, ldb) => (actorRef, ldb) }  
  val InitializedSeed(ahoCorasick, ldb) = ldbEnginePooling(inputCSVfileName) 
  
  //
  // Public interface to process a document 
  //                                        
  def process(cleanJATSaccess: com.articlio.dataExecution.concrete.JATSaccess)(dataID: Long, dataType: String, articleName: String) : Option[CreateError] = {

    val document = new JATS(s"${cleanJATSaccess.dirPath}/$articleName.xml")
    
    val logger = new LdbEngineDocumentLogger(document.name)
    
    //
    // get data
    //
    val sections : Seq[JATSsection] = document.sections // elife04395, elife-articles(XML)/elife00425styled.xml
    //if (sections.isEmpty) return s"${document.name} appears to have no sections and was not processed"  
    if (sections.isEmpty) return Some(CreateError("no sections detected in JATS input - cannot extract")) // Seq.empty[Matches]  
    
    //
    // Enriching the string type with custom method. TODO: separate into util package object?
    //
    implicit class MyStringOps(val s: String) {  
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
        val matchedFragmentsFuture = ask(ahoCorasick, ProcessSentenceMessage(sentence.text, logger)).mapTo[List[Map[String, String]]]
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
          val fragmentPatterns = ldb.fragments2patterns.get(matched("match").toString).get
          possiblePatternMatches ++= fragmentPatterns
        })

        // 
        // decides what to extract. for now extracts the entire sentence,
        // or the entire sentence plus the next one up (in case of special type of cataphora).
        //
        def extraction(sentence: LocatedText) : LocatedText = {
          if (sentence.text.endsWithAny(Seq(":", "the following.", "as follows."))) {
            val resultSpan = sentence.text + " " + sentences(sentenceIdx+1).text // TODO: assure not out of bounds
            logger.write(document.name + ": "  + resultSpan + "\n", "overall-salient-matches")
            return LocatedText(resultSpan, sentence.section)
          }
          else 
            return sentence
        }
        
        case class PossibleMatch(pattern : String, locatedText: LocatedText, indication: String, simpleRule: SimpleRule)
        val        possibleMatches = for (pat <- possiblePatternMatches.result 
                                if (isInOrder (ldb.patterns2fragments.get(pat).get, -1))) 
                                  yield ( new PossibleMatch(
                                                pattern = pat,
                                                locatedText = extraction(sentence),
                                                indication = ldb.patterns2indications.get(pat).get,
                                                simpleRule =ldb.patterns2rules(pat)))

        possibleMatches.foreach(p =>
          logger.write(Seq(s"sentence '${p.locatedText.text}'",
                           s"in section ${p.locatedText.section}",
                           s"matches pattern '${p.pattern}'",
                           s"which indicates '${p.indication}'").mkString("\n") + "\n","sentence-pattern-matches (location agnostic)"))

        //if (!possibleMatches.isEmpty)logger.write(sentence.text, "output (location agnostic)")
                          
        //
        // checks whether a potential match satisfies its location criteria if any
        // 
        
        def locationTest(p: PossibleMatch) : Boolean = {
            var isFinalMatch = false
                if (!p.simpleRule.locationProperty.isDefined) {
                  isFinalMatch = true
                }
                else if (p.simpleRule.locationProperty.get.head.asInstanceOf[LocationProperty].parameters.exists(parameter =>   // 'using .head' assumes at most one LocationProperty per rule
                sectionTypeScheme.translation.contains(parameter.toLowerCase) && sectionTypeScheme.translation(parameter.toLowerCase) == p.locatedText.section.toLowerCase())) {
                  //println("location criteria matched!")
                  isFinalMatch = true
                }
                else {
                  isFinalMatch = false            
                      if (false)
                      {
                        println
                        println("location criteria not matched for:")
                        println(p.locatedText.text)
                        println("should be in either:")
                        p.simpleRule.locationProperty.get.head.asInstanceOf[LocationProperty].parameters.foreach(parameter =>   // 'using .head' assumes at most one LocationProperty per rule
                        if (sectionTypeScheme.translation.contains(parameter.toLowerCase)) println(sectionTypeScheme.translation(parameter.toLowerCase)))
                        println("but found in:")
                        println(p.locatedText.section)
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
        
        def selfishRefTest(p: PossibleMatch) : Boolean = {
          var isFinalMatch = false
        	
          if (!p.simpleRule.ReferenceProperty.isDefined) isFinalMatch = true         
        	else isFinalMatch = containsSelfRef(p.simpleRule.pattern) match {
        	  case true => true // if rule's pattern already contains a selfish reference itself, this is a match
        	  case false => containsSelfRef(p.locatedText.text) 
        	}
          
        	return isFinalMatch
        }
        
	      val tentativeMatches = 
	        possibleMatches.map(p => new { val pattern = p.pattern; 
                                         val locatedText = p.locatedText;
                                         val indication = p.indication;
                                         val simpleRule = p.simpleRule;
                                         val matchesLocation = locationTest(p);
                                         val selfishRef = selfishRefTest(p) }).toSeq
     	
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
          tentativeMatches.filter(_.selfishRef).map(m => MatchesRow(dataID,
                            document.name, 
                            m.locatedText.text, 
                  				  m.pattern,
                  				  m.simpleRule.locationProperty.isEmpty match {
                  				  	case true  => "any"
                  				  	case false => m.simpleRule.locationProperty.get.head.asInstanceOf[LocationProperty].parameters.mkString(" | ")
                  				  },
                  				  m.locatedText.section,
                  				  m.matchesLocation,
                  				  m.indication))
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
