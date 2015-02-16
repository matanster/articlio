package com.articlio.ldb
import com.articlio.config
import com.articlio.util._
import com.articlio.util.text._
import com.articlio.semantic.AppActorSystem
//import com.articlio.util.{wordFollowing}

import scala.io.Source
//import java.net.URLEncoder
//import spray.json._
//import DefaultJsonProtocol._
import org.ahocorasick.trie._
//import scala.collection.JavaConversions._ // work with Java collections as if they were Scala
import scala.collection.JavaConverters._ // convert Java colllections to Scala ones
import com.github.tototoshi.csv._ // only good for "small" csv files; https://github.com/tototoshi/scala-csv/issues/11
//import scala.collection.mutable.MutableList
//import org.apache.commons.math3           // for using descriptive statistics over collections
import com.github.verbalexpressions.VerbalExpression._

//
// structure for input CSV representation
//
case class RawCSVInput(pattern: String, indication: String, parameters: Seq[String])

//
// class hierarchy for describing rules as derived by the CSV object
//
abstract class Property {
  val subType: Symbol
  val necessityModality: Symbol
}

case class ReferenceProperty(subType: Symbol,
  necessityModality: Symbol)
  extends Property

case class LocationProperty(subType: Symbol,
  parameters: Seq[String],
  necessityModality: Symbol)
  extends Property

case class RuleInput(pattern: String, indication: String, properties: Option[Seq[Property]])

object CSV {

  //
  // Extract raw values from the database CSV
  // Slow (https://github.com/tototoshi/scala-csv/issues/11) but global initialization is currently insignificant
  //
  def loadPatternsFromCSV(csvFile: String): Seq[RawCSVInput] = {

    AppActorSystem.timelog ! "reading CSV"

    //val reader = CSVReader.open("ldb/July 24 2014 database - Markers - filtered.csv")
    //val reader = CSVReader.open("ldb/Normalized from July 24 2014 database - Markers - filtered - take 1.csv")
    //ldb/Normalized from July 24 2014 database - Dec 30 - plus Jan tentative addition.csv
    val reader = CSVReader.open(config.ldb + "/" + csvFile)
    val iterator = reader.iterator
    iterator.next // skip first row assumed to be headers

    var rawInput = Seq[RawCSVInput]()
    var totalOff: Int = 0

    while (iterator.hasNext) {
      val asArray: Array[String] = iterator.next.toArray // convert to Array for easy column access
      val pattern = asArray(4)
      val indication = asArray(5)
      val parameters: Seq[String] = Seq(asArray(6), asArray(7), asArray(8)) // additional parameters expressed in the database CSV

      val off = (asArray(2) == "off")

      if (off)
        totalOff += 1
      else
        rawInput = rawInput :+ new RawCSVInput(pattern, indication, parameters)
    }

    if (totalOff > 0) println(s"$totalOff rules disabled in input CSV, see input CSV for details")

    AppActorSystem.timelog ! "reading CSV"
    reader.close

    rawInput
  }

  //
  // build rules from the raw CSV input rows
  //
  def deriveFromCSV(csvFile: String): Seq[RuleInput] = {
    
    //
    // ldb beefer: add abstract location criteria wherever introduction and conclusion are included
    //
    def expandSectionRequirementsAbstract(parameter: String): Option[Seq[String]] = {
      val baseLocationParams = wordFollowingAny(parameter, Seq("in ", "or in "))
      baseLocationParams match {
        case None => None
        case Some(s) => {
          s.foreach(b => 
          	if (b.contains("introduction") || b.contains("conclusion")) {
          	  return Some(s :+ "abstract")
          	})
          return Some(s)
        }
      }
    }

    //
    // ldb beefer: allow limitations section location, wherever rule indicates limitation, and has some location criteria 
    //
    def expandSectionRequirementsLimitations(rule: RawCSVInput, sections: Option[Seq[String]]): Option[Seq[String]] = {
      sections match {
        case None => None
        case Some(s) => {
          if (rule.indication == "limitation")
            return Some(s :+ "limitation")
          return sections
        }
      }
    }
  
    AppActorSystem.timelog ! "manipulating CSV input"

    val rules = scala.collection.mutable.Seq.newBuilder[RuleInput]
    val rawInput = loadPatternsFromCSV(csvFile)

    rawInput map { rawInputRule =>

      val ruleProperties = scala.collection.mutable.Seq.newBuilder[Property]

      rawInputRule.parameters filter (_.nonEmpty) foreach (parameter => {

        val selfRef: Boolean = parameter.containsSlice("self ref")
        val deicticRef: Boolean = parameter.containsSlice("deictic")
        val youRef: Boolean = parameter.containsSlice("\"you\"")
        val baseSections: Option[Seq[String]] = expandSectionRequirementsAbstract(parameter)
        val modality: Symbol = if (parameter.containsSlice("no ") | parameter.containsSlice("not ")) 'mustNot
        else 'must

        val sections = expandSectionRequirementsLimitations(rawInputRule, baseSections)
        
        if (selfRef) ruleProperties += ReferenceProperty('AuthorSelf, modality)
        if (deicticRef) ruleProperties += ReferenceProperty('DocumentSelf, modality)
        if (youRef) ruleProperties += ReferenceProperty('You, modality)
        if (sections.isDefined) ruleProperties += LocationProperty('inside, sections.get, modality)

      })

      if (rawInputRule.pattern == "although") {
        println(rawInputRule.parameters)
        println(ruleProperties)
      }
      rules += new RuleInput(rawInputRule.pattern, rawInputRule.indication, if (ruleProperties.result.nonEmpty) Some(ruleProperties.result) else None)
    }

    AppActorSystem.timelog ! "manipulating CSV input"
    val logger = new Logger("global-ldb-csv")
    logger.write(rules.result.mkString("\n"), "db-rules")
    return rules.result
  }
}
