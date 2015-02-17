import com.articlio.dataExecution.concrete._

abstract class a(a: String)
case class b(a: String) extends a(a: String)
case class c(a: String) extends a(a: String)

object test {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(199); 
  val v = new b("b");System.out.println("""v  : b = """ + $show(v ));$skip(141); 
   v match {
    case _:bd => println("specific")
    case dSFAF => println("this is unexpected")
    case ad => println("not specific")
  }}
}
