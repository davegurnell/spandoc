package spandoc.ast

final case class Attr(id: String, classes: List[String] = Nil, attr: List[(String, String)] = Nil)

object Attr {
  val empty = Attr("", Nil, Nil)
}
