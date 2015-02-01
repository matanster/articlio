package com.articlio.util

//
// this is a package object for elegant access 
// (as per http://www.scala-lang.org/docu/files/packageobjects/packageobjects.html)
//
package object text { 
  def SPACE = " " // use to allow finding all spaces mentioned in code! 

  def FIRST_ORDINALS = Seq("first", "second", "third", "forth", "fifth", "sixth", "seventh", "eighth",
                           "1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th")

  // 
  // turns text into Sentence case. 
  // should decapitalize first word only if its capitalization is due to its location 
  // Limitation: current implementation only partially handles figuring if first word's capitalization is due to location or not.
  //
  // uses http://www.scala-lang.org/api/current/index.html#scala.Char.toLower
  // with some refactor, toLowerCase can be used instead if need to override machine locale
  //
  def deSentenceCase (text: String) : String = {
    if (0 == text.indexOfSlice('I' + SPACE)) return text
    return text.head.toLower + text.tail
  }

  def splitToWords = (word: String) => word.split(SPACE)
  
  def wordFollowing(text: String, following: String) : Option[String] = {
    if (text.containsSlice(following)) Some(text.drop(text.indexOfSlice(following) + following.length).takeWhile(char => !(Seq(' ', '.') contains char)))
    else None
  }

  def wordFollowingAny(text: String, following: Seq[String]) : Option[Seq[String]] = {
    val found = following map { f => wordFollowing(text, f) } filter(_.isDefined) map {_.get}
    if (found.isEmpty) None 
    else Some(found)
  }
}
