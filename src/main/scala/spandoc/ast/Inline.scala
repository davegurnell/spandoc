package spandoc.ast

sealed abstract class Inline
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
final case class Image(attr: Attr, inlines: Vector[Inline], target: Target) extends Inline
final case class Note(blocks: Vector[Block]) extends Inline
final case class Span(attr: Attr, inlines: Vector[Inline]) extends Inline

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
