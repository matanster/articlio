abstract class A(some: String)
case class B(some: String) extends A(some: String)
case class C(some: String) extends A(some: String)

object tester {
  def test(a: A) {
   a match {
    case _:B => println("B")
    case _:C => println("C")
   }
  }
  
  val tester = test(B("some"))
}
