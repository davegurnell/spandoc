package spandoc.ast

final case class Meta(data: Map[String, MetaValue]) {
  def get(head: String, tail: String*): Option[MetaValue] =
    data.get(head).flatMap(_.get(tail: _*))
}

object Meta {
  val empty = Meta(Map.empty)
}

sealed abstract class MetaValue {
  def get(path: String*): Option[MetaValue] =
    path match {
      case Nil => Some(this)
      case head :: tail =>
        this match {
          case MetaMap(data) => data.get(head).flatMap(_.get(tail: _*))
          case _             => None
        }
    }
}

final case class MetaMap(values: Map[String, MetaValue]) extends MetaValue
final case class MetaList(values: Vector[MetaValue]) extends MetaValue
final case class MetaBool(value: Boolean) extends MetaValue
final case class MetaString(value: String) extends MetaValue
final case class MetaInlines(inlines: Vector[Inline]) extends MetaValue
final case class MetaBlocks(blocks: Vector[Block]) extends MetaValue
