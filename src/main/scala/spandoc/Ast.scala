package spandoc

final case class Pandoc(blocks: Vector[Block], meta: Meta = Meta.empty, apiVersion: PandocApiVersion = Pandoc.defaultVersion)

final case class PandocApiVersion(major: Int, minor: Int)

object Pandoc {
  val defaultVersion: PandocApiVersion =
    PandocApiVersion(1, 20)

  def apply(blocks: Vector[Block]): Pandoc =
    Pandoc(blocks, Meta.empty, defaultVersion)
}

final case class Meta(data: Map[String, MetaValue])

object Meta {
  val empty = Meta(Map.empty)
}

sealed abstract class MetaValue
final case class MetaMap(values: Map[String, MetaValue]) extends MetaValue
final case class MetaList(values: Vector[MetaValue]) extends MetaValue
final case class MetaBool(value: Boolean) extends MetaValue
final case class MetaString(value: String) extends MetaValue
final case class MetaInlines(inlines: Vector[Inline]) extends MetaValue
final case class MetaBlocks(blocks: Vector[Block]) extends MetaValue

sealed abstract class Node

sealed abstract class Block extends Node
final case class Plain(inlines: Vector[Inline]) extends Block
final case class Para(inlines: Vector[Inline]) extends Block
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
case object Null extends Block

object Header {
  def apply(level: Int, inlines: Vector[Inline]): Header =
    Header(level, Attr.empty, inlines)
}

sealed abstract class Inline extends Node
final case class Str(text: String) extends Inline
final case class Emph(inlines: Vector[Inline]) extends Inline
final case class Strong(inlines: Vector[Inline]) extends Inline
final case class Strikeout(inlines: Vector[Inline]) extends Inline
final case class Superscript(inlines: Vector[Inline]) extends Inline
final case class Subscript(inlines: Vector[Inline]) extends Inline
final case class SmallCaps(inlines: Vector[Inline]) extends Inline
final case class Quoted(tpe: QuoteType, inlines: Vector[Inline]) extends Inline
final case class Cite(citations: Vector[Citation], inlines: Vector[Inline]) extends Inline
final case class Code(attr: Attr, text: String) extends Inline
case object Space extends Inline
case object SoftBreak extends Inline
case object LineBreak extends Inline
final case class Math(tpe: MathType, text: String) extends Inline
final case class RawInline(format: String, text: String) extends Inline
final case class Link(attr: Attr, inlines: Vector[Inline], target: Target) extends Inline
final case class Image(inlines: Vector[Inline], target: Target) extends Inline
final case class Note(blocks: Vector[Block]) extends Inline
final case class Span(attr: Attr, inlines: Vector[Inline]) extends Inline

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

final case class Attr(id: String, classes: Vector[String] = Vector.empty, attr: Vector[(String, String)] = Vector.empty)

object Attr {
  val empty = Attr("", Vector.empty, Vector.empty)
}

final case class TableRow(cells: Vector[TableCell])
final case class TableCell(blocks: Vector[Block])

sealed abstract class QuoteType
case object SingleQuote extends QuoteType
case object DoubleQuote extends QuoteType

final case class Target(url: String, title: String)

sealed abstract class MathType
case object DisplayMath extends MathType
case object InlineMath extends MathType

final case class Citation(
  id: String,
  prefix: Vector[Inline],
  suffix: Vector[Inline],
  mode: CitationMode,
  noteNum: Int,
  hash: Int
)

sealed abstract class CitationMode
final case object Constructors extends CitationMode
final case object AuthorInText extends CitationMode
final case object SuppressAuthor extends CitationMode
final case object NormalCitation extends CitationMode
