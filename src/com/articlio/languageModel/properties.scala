//
// Modeling for expansion of asr patterns.
// Has no effect as long as no {asr phrases in patterns database}
// See properties2.scala for better but not yet in use modelling
//

package com.articlio.LanguageModel
//import com.articlio.LanguageModel._
//import com.articlio.LanguageModel.LinguisticProperty._
import com.articlio.util.text._

object SelfishReferences {

  //
  // article-self-reference triggers (noun phrases all)
  //
  object ArticleDirectSelfReference {

    // determiners and their determiner type (as per ontology at http://en.wikipedia.org/wiki/Determiner_phrase#The_competing_analyses)
    private val reference = Set( 
      AnnotatedL1("the present", Set(Determiner)),
      AnnotatedL1("this",        Set(Determiner, Demonstrative)),
      AnnotatedL1("our",         Set(Determiner, Possesive)),
      AnnotatedL1("my",          Set(Determiner, Possesive)),
      AnnotatedL1("the current", Set(Determiner)))

    // nouns
    private val referencedAs = Set(
      AnnotatedL1("study"),
      AnnotatedL1("article"),
      AnnotatedL1("section"),
      AnnotatedL1("chapter"),
      AnnotatedL1("unit"),
      AnnotatedL1("paper"),
      AnnotatedL1("research"),
      AnnotatedL1("work"),
      AnnotatedL1("essay"),
      AnnotatedL1("note"),
      AnnotatedL1("report"),
      AnnotatedL1("experiment"),
      AnnotatedL1("series of experiments"),
      AnnotatedL1("project"),
      AnnotatedL1("subsection"),
      AnnotatedL1("review"),
      AnnotatedL1("draft"),
      AnnotatedL1("project"))

    //val references : Set[String] = for (firstHalf <- reference; secondHalf <- referencedAs) yield (firstHalf.text + SPACE + secondHalf.text)
    val references : Set[AnnotatedL2] = for (firstHalf <- reference; secondHalf <- referencedAs) 
                                         yield AnnotatedL2(AnnotatedL1(firstHalf.text + SPACE + secondHalf.text, Set(NounPhrase)), SelfWork)

    //
    // sketchy method for figuring if sentence contains an article self reference
    //
    //def has (sentence: String) : Boolean = references.exists(reference => deSentenceCase(sentence).containsSlice(reference))

  }

  //
  // author-self-reference triggers
  //
  object ArticleAuthorSelfReference {

    // - to be categorized/tagged by their different properties such as inclusion/disclusion of a possesive(genetive) property.
    val references = Set(
      AnnotatedL1("I",           Set(PersonalPronoun, Personal, SubjectForm)),
      AnnotatedL1("we",          Set(PersonalPronoun, Personal, SubjectForm)),

      AnnotatedL1("me",          Set(PersonalPronoun, Personal, ObjectForm)),
      //AnnotatedL1("myself",      Set(PersonalPronoun, ObjectForm)),
      AnnotatedL1("us",          Set(PersonalPronoun, Personal, ObjectForm)),

      AnnotatedL1("the author",  Set(Personal)),
      AnnotatedL1("the authors", Set(Personal, Plural)),
      AnnotatedL1("my",          Set(PossesivePronoun, Possesive)),
      AnnotatedL1("our",         Set(PossesivePronoun, Possesive)))

    val references2 : Set[AnnotatedL2] = references map (annotated => AnnotatedL2(AnnotatedL1(annotated.text, annotated.properties), SelfAuthor))

    //
    // sketchy method for figuring if sentence contains an author self reference
    //
    //def has (sentence: String) : Boolean = references.exists(reference => deSentenceCase(sentence).containsSlice(reference.text))
  }

  //val refs : Set[String] = ArticleDirectSelfReference.references ++ (ArticleAuthorSelfReference.references map (reference => reference.text))
  val all: Set[AnnotatedL2] = ArticleDirectSelfReference.references ++ ArticleAuthorSelfReference.references2
  val allTexts : Set[String] = all map (ref => ref.annotatedL1.text)
}