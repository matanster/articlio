//
// Modeling for better expansion of asr patterns.
// Not yet in use.
// See properties.scala for previous worse yet working expansion modelling
//

package com.articlio.languageModel2
import com.articlio.util.text._

abstract class PhraseProperty
abstract class EmbeddingProjection

abstract class PluralityType extends PhraseProperty
case object    Singular      extends PluralityType
case object    Plural        extends PluralityType

abstract class EntityType        extends PhraseProperty
case object    WorkEntityType    extends EntityType
abstract class PersonaEntityType extends EntityType
case object    PersonaEntityType extends PersonaEntityType
case object    PersonalPronoun   extends PersonaEntityType with PersonalPronounForm

trait       PersonalPronounForm
case object Subject extends PersonalPronounForm
case object Object  extends PersonalPronounForm

case class ProjectedForm(form: String) extends EmbeddingProjection

class  Modulator (val modulator: String => String) extends EmbeddingProjection 
object Modulator {def apply(modulator: String => String) = new Modulator(modulator)} // class constructor

//
// Common way to augment present tense verb stem
//
case object CommonPresentTenseModulator extends Modulator((presentTenseVerb: String) => 
  // handles special case of present tenses that end with 's'
  presentTenseVerb.endsWith("s") match {
    case true  => presentTenseVerb + "es"
    case false => presentTenseVerb + "s"
  }
)

//
// Most common way to add possessive relationship connective at end of noun phrase
//
case object CommonPossesiveModulator extends Modulator((selfText: String) =>
  // handles special case of noun phrases that end with 's'
  selfText.endsWith("s") match {
    case true  => selfText + "'"
    case false => selfText + "'s"
  }
)

case class NounPhrase (
  baseForm                  : String,
  pluralityType             : PluralityType, 
  entityType                : EntityType, 
  possesiveForm             : Modulator = CommonPossesiveModulator,
  presentTenseVerbModulator : Modulator = CommonPresentTenseModulator

)

object SelfReferences {

  //
  // article-self-reference noun phrases
  //
  object ArticleSelfReference {

    // determiners and their determiner type (as per ontology at http://en.wikipedia.org/wiki/Determiner_phrase#The_competing_analyses)
    private val reference = Set( 
      "the present", 
      "this",        
      "our",       
      "my",        
      "the current"
    )

    // nouns
    private val referencedAs = Set(
      "study",
      "article",
      "section",
      "chapter",
      "unit",
      "paper",
      "research",
      "work",
      "essay",
      "note",
      "report",
      "experiment",
      "series of experiments",
      "project",
      "subsection",
      "review",
      "draft",
      "project"
    )

    val references : Set[NounPhrase] = for (firstHalf <- reference; secondHalf <- referencedAs) 
                                         yield NounPhrase(firstHalf + SPACE + secondHalf, Singular, WorkEntityType)
  }

  //
  // author-self-reference triggers
  //
  object ArticleAuthorSelfReference {

    // - to be categorized/tagged by their different properties such as inclusion/disclusion of a possesive(genetive) property.
    val references = Set(
      NounPhrase("I", Singular, PersonaEntityType, possesiveForm = Modulator(_ => "My"), presentTenseVerbModulator = Modulator(identity[String])), 
      NounPhrase("we", Plural, PersonaEntityType, possesiveForm = Modulator(_ => "Our"), presentTenseVerbModulator = Modulator(identity[String])),
      NounPhrase("the author",  Singular, PersonaEntityType),
      NounPhrase("the authors", Plural, PersonaEntityType, presentTenseVerbModulator = Modulator(identity[String]))
    )
  }

  val references = ArticleSelfReference.references ++ ArticleAuthorSelfReference.references

}