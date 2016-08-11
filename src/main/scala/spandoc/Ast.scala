package spandoc

final case class Pandoc(meta: Meta, blocks: List[Block])

final case class Meta(data: Map[String, MetaValue])

sealed abstract class MetaValue
final case class MetaMap(values: Map[String, MetaValue]) extends MetaValue
final case class MetaList(values: List[MetaValue]) extends MetaValue
final case class MetaBool(value: Boolean) extends MetaValue
final case class MetaString(value: String) extends MetaValue
final case class MetaInlines(inlines: List[Inline]) extends MetaValue
final case class MetaBlocks(blocks: List[Block]) extends MetaValue

sealed abstract class Node

sealed abstract class Block extends Node
final case class Plain(inlines: List[Inline]) extends Block
final case class Para(inlines: List[Inline]) extends Block
final case class CodeBlock(attr: Attr, text: String) extends Block
final case class RawBlock(format: String, text: String) extends Block
final case class BlockQuote(blocks: List[Block]) extends Block
final case class OrderedList(attr: ListAttributes, items: List[ListItem]) extends Block
final case class BulletList(items: List[ListItem]) extends Block
final case class DefinitionList(items: List[DefinitionItem]) extends Block
final case class Header(level: Int, attr: Attr, inlines: List[Inline]) extends Block
case object HorizontalRule extends Block
final case class Table(
  caption: List[Inline],
  columnAlignments: List[Alignment],
  columnWidths: List[Double],
  columnHeaders: List[TableCell],
  rows: List[TableRow]
) extends Block
final case class Div(attr: Attr, blocks: List[Block]) extends Block
case object Null extends Block

sealed abstract class Inline extends Node
final case class Str(text: String) extends Inline
final case class Emph(inlines: List[Inline]) extends Inline
final case class Strong(inlines: List[Inline]) extends Inline
final case class Strikeout(inlines: List[Inline]) extends Inline
final case class Superscript(inlines: List[Inline]) extends Inline
final case class Subscript(inlines: List[Inline]) extends Inline
final case class SmallCaps(inlines: List[Inline]) extends Inline
final case class Quoted(tpe: QuoteType, inlines: List[Inline]) extends Inline
final case class Cite(citations: List[Citation], inlines: List[Inline]) extends Inline
final case class Code(attr: Attr, text: String) extends Inline
case object Space extends Inline
case object SoftBreak extends Inline
case object LineBreak extends Inline
final case class Math(tpe: MathType, text: String) extends Inline
final case class RawInline(format: String, text: String) extends Inline
final case class Link(inlines: List[Inline], target: Target) extends Inline
final case class Image(inlines: List[Inline], target: Target) extends Inline
final case class Note(blocks: List[Block]) extends Inline
final case class Span(attr: Attr, inlines: List[Inline]) extends Inline

sealed abstract class Alignment
case object AlignLeft extends Alignment
case object AlignRight extends Alignment
case object AlignCenter extends Alignment
case object AlignDefault extends Alignment

// TODO: Check the first argument is actually a level:
final case class ListAttributes(level: Int, style: ListNumberStyle, delim: ListNumberDelim)

final case class ListItem(blocks: List[Block])

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

final case class DefinitionItem(term: List[Inline], definitions: List[Definition])
final case class Definition(blocks: List[Block])

final case class Attr(id: String, classes: List[String], attr: List[(String, String)])

final case class TableRow(cells: List[TableCell])
final case class TableCell(blocks: List[Block])

sealed abstract class QuoteType
case object SingleQuote extends QuoteType
case object DoubleQuote extends QuoteType

final case class Target(url: String, title: String)

sealed abstract class MathType
case object DisplayMath extends MathType
case object InlineMath extends MathType

final case class Citation(
  id: String,
  prefix: List[Inline],
  suffix: List[Inline],
  mode: CitationMode,
  noteNum: Int,
  hash: Int
)

sealed abstract class CitationMode
final case object Constructors extends CitationMode
final case object AuthorInText extends CitationMode
final case object SuppressAuthor extends CitationMode
final case object NormalCitation extends CitationMode
