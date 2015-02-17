import com.articlio.dataExecution.concrete._

abstract class A(some: String)
case class B(some: String) extends A(some: String)
case class C(some: String) extends A(some: String)

class test(a: A) {
   a match {
    case _:B => println("B")
    case _:C => println("C")
  }
}

object tester {;import org.scalaide.worksheet.runtime.library.WorksheetSupport._; def main(args: Array[String])=$execute{;$skip(327); 
  val tester = new test(B("some"));System.out.println("""tester  : test = """ + $show(tester ))}
}
