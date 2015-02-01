package com.articlio.ldb

//
// TODO: This is rather very ugly, a function that checks a map and separately handles the cases not covered in the map,
//       may make better sense. This is convoluted as is.
//

abstract class sectionTypeScheme {
  val translation: Map[String, String]
}

object eLifeSectionTypeScheme extends sectionTypeScheme {
  val translation = Map("introduction" -> "intro",
                        "discussion" -> "discussion",
                        "conclusion" -> "discussion",
                        "limitation" -> "limitation",
                        "abstract" -> "abstract")
}

object pdfConvertedSectionTypeScheme extends sectionTypeScheme {
  val translation = Map("introduction" -> "introduction",
                        "discussion" -> "discussion",
                        "conclusion" -> "conclusion",
                        "limitation" -> "limitation",                        
                        "abstract" -> "abstract")
}

