case class JATS(articleName: String, dataName: String = "default")
{
}


object aa {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(128); 
  println("Welcome to the Scala worksheet");$skip(21); 
  val a = JATS("aa");System.out.println("""a  : JATS = """ + $show(a ));$skip(13); val res$0 = 
  a.dataName;System.out.println("""res0: String = """ + $show(res$0))}
}
