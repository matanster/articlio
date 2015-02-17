package com.articlio.dataExecution.concrete

object test {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(71); 
println("A");$skip(162); 
  val b = specificSemanticData("a", "b")
   match {
    case _:specificSemanticData => println("specific")
    case _:SemanticData => println("not specific")
  };System.out.println("""b  : Unit = """ + $show(b ))}
}
