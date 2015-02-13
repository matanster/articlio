case class JATS(articleName: String, dataName: String = "default")
{
}

object aa {
  println("Welcome to the Scala worksheet")
  val a = JATS("aa")
  a.dataName
  a.getClass
}