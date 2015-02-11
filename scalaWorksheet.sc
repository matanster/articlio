object aa {
  println("Welcome to the Scala worksheet")       //> Welcome to the Scala worksheet
  import scala.util.control.Exception._
  import java.net._

  val s = "http://www.scala-lang.org/"            //> s  : String = http://www.scala-lang.org/
  val x1 = catching(classOf[MalformedURLException]) opt new URL(s)
                                                  //> x1  : Option[java.net.URL] = Some(http://www.scala-lang.org/)
  val x2 = catching(classOf[MalformedURLException], classOf[NullPointerException]) either new URL(s)
                                                  //> x2  : scala.util.Either[Throwable,java.net.URL] = Right(http://www.scala-lan
                                                  //| g.org/)
}