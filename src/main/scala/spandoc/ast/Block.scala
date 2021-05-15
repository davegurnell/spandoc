package spandoc.ast

sealed abstract class Block
final case class Plain(inlines: Vector[Inline]) extends Block
final case class Para(inlines: Vector[Inline]) extends Block
final case class LineBlock(inlines: Vector[Vector[Inline]]) extends Block
final case class CodeBlock(attr: Attr, text: String) extends Block
final case class RawBlock(format: String, text: String) extends Block
final case class BlockQuote(blocks: Vector[Block]) extends Block
final case class OrderedList(attr: ListAttributes, items: Vector[ListItem]) extends Block
final case class BulletList(items: Vector[ListItem]) extends Block
final case class DefinitionList(items: Vector[DefinitionItem]) extends Block
final case class Header(level: Int, attr: Attr, inlines: Vector[Inline]) extends Block
case object HorizontalRule extends Block
final case class Table(
  caption: Vector[Inline],
  columnAlignments: Vector[Alignment],
  columnWidths: Vector[Double],
  columnHeaders: Vector[TableCell],
  rows: Vector[TableRow]
) extends Block

final case class Div(attr: Attr, blocks: Vector[Block]) extends Block

object Div {
  val tupled = (apply _).tupled
  val empty: Div = Div(Attr.empty, Vector.empty)
}

case object Null extends Block

object Header {
  def apply(level: Int, inlines: Vector[Inline]): Header =
    Header(level, Attr.empty, inlines)
}

sealed abstract class Alignment
case object AlignLeft extends Alignment
case object AlignRight extends Alignment
case object AlignCenter extends Alignment
case object AlignDefault extends Alignment

// TODO: Check the first argument is actually a level:
final case class ListAttributes(level: Int, style: ListNumberStyle, delim: ListNumberDelim)

final case class ListItem(blocks: Vector[Block])

sealed abstract class ListNumberStyle
case object DefaultStyle extends ListNumberStyle
case object Example extends ListNumberStyle
case object Decimal extends ListNumberStyle
case object LowerRoman extends ListNumberStyle
case object UpperRoman extends ListNumberStyle
case object LowerAlpha extends ListNumberStyle
case object UpperAlpha extends ListNumberStyle

sealed abstract class ListNumberDelim
case object DefaultDelim extends ListNumberDelim
case object Period extends ListNumberDelim
case object OneParen extends ListNumberDelim
case object TwoParens extends ListNumberDelim

final case class DefinitionItem(term: Vector[Inline], definitions: Vector[Definition])
final case class Definition(blocks: Vector[Block])

final case class TableRow(cells: Vector[TableCell])
final case class TableCell(blocks: Vector[Block])
