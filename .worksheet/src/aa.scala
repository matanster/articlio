object aa {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(55); 
  println("Welcome to the Scala worksheet")
  import scala.util.control.Exception._
  import java.net._;$skip(100); 

  val s = "http://www.scala-lang.org/";System.out.println("""s  : String = """ + $show(s ));$skip(67); 
  val x1 = catching(classOf[MalformedURLException]) opt new URL(s);System.out.println("""x1  : Option[java.net.URL] = """ + $show(x1 ));$skip(101); 
  val x2 = catching(classOf[MalformedURLException], classOf[NullPointerException]) either new URL(s);System.out.println("""x2  : scala.util.Either[Throwable,java.net.URL] = """ + $show(x2 ))}
}
