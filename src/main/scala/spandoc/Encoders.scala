package spandoc

import cats.data._
import cats.syntax.all._
import io.circe._
import io.circe.syntax._

trait Encoders extends EncoderHelpers {
  // TODO: Pandoc
  // TODO: Meta
  // TODO: MetaValue

  implicit def BlockEncoder: Encoder[Block] =
    Encoder.instance[Block] {
      case Plain(inlines)               => typedNode("Plain")(inlines)
      case Para(inlines)                => typedNode("Para")(inlines)
      case CodeBlock(attr, text)        => typedNode("CodeBlock")((attr, text))
      case RawBlock(format, text)       => typedNode("RawBlock")((format, text))
      case BlockQuote(blocks)           => typedNode("BlockQuote")(blocks)
      case OrderedList(attr, items)     => typedNode("OrderedList")((attr, items))
      case BulletList(items)            => typedNode("BulletList")(items)
      case DefinitionList(items)        => typedNode("DefinitionList")(items)
      case Header(level, attr, inlines) => typedNode("Header")((level, attr, inlines))
      case HorizontalRule               => typedNode("HorizontalRule")(Json.arr())
      case Table(c, a, w, h, r)         => typedNode("Table")((c, a, w, h, r))
      case Div(attr, blocks)            => typedNode("Div")((attr, blocks))
      case Null                         => typedNode("Null")(Json.arr())
    }

  implicit def InlineEncoder: Encoder[Inline] =
    Encoder.instance[Inline] {
      case Str(text)                => typedNode("Str")(text)
      case Emph(inlines)            => typedNode("Emph")(inlines)
      case Strong(inlines)          => typedNode("Strong")(inlines)
      case Strikeout(inlines)       => typedNode("Strikeout")(inlines)
      case Superscript(inlines)     => typedNode("Superscript")(inlines)
      case Subscript(inlines)       => typedNode("Subscript")(inlines)
      case SmallCaps(inlines)       => typedNode("SmallCaps")(inlines)
      case Quoted(tpe, inlines)     => typedNode("Quoted")((tpe, inlines))
      case Cite(citations, inlines) => typedNode("Cite")((citations, inlines))
      case Code(attr, text)         => typedNode("Code")((attr, text))
      case Space                    => typedNode("Space")(Json.arr())
      case SoftBreak                => typedNode("SoftBreak")(Json.arr())
      case LineBreak                => typedNode("LineBreak")(Json.arr())
      case Math(tpe, text)          => typedNode("Math")((tpe, text))
      case RawInline(format, text)  => typedNode("RawInline")((format, text))
      case Link(inlines, target)    => typedNode("Link")((inlines, target))
      case Image(inlines, target)   => typedNode("Image")((inlines, target))
      case Note(blocks)             => typedNode("Note")(blocks)
      case Span(attr, inlines)      => typedNode("Span")((attr, inlines))
    }

  implicit def AlignmentEncoder: Encoder[Alignment] =
    stringNodeEncoder[Alignment]

  implicit def ListAttributesEncoder: Encoder[ListAttributes] =
    Encoder[(Int, ListNumberStyle, ListNumberDelim)].contramap(unlift(ListAttributes.unapply))

  implicit def ListItemEncoder: Encoder[ListItem] =
    Encoder[List[Block]].contramap(unlift(ListItem.unapply))

  implicit def ListNumberStyleEncoder: Encoder[ListNumberStyle] =
    stringNodeEncoder[ListNumberStyle]

  implicit def ListNumberDelimEncoder: Encoder[ListNumberDelim] =
    stringNodeEncoder[ListNumberDelim]

  implicit def DefinitionItemEncoder: Encoder[DefinitionItem] =
    Encoder[(List[Inline], List[Definition])].contramap(unlift(DefinitionItem.unapply))

  implicit def DefinitionEncoder: Encoder[Definition] =
    Encoder[List[Block]].contramap(unlift(Definition.unapply))

  implicit def AttrEncoder: Encoder[Attr] =
    Encoder[(String, List[String], List[(String, String)])].contramap(unlift(Attr.unapply))

  implicit def TableRowEncoder: Encoder[TableRow] =
    Encoder[List[TableCell]].contramap(unlift(TableRow.unapply))

  implicit def TableCellEncoder: Encoder[TableCell] =
    Encoder[List[Block]].contramap(unlift(TableCell.unapply))

  implicit def QuoteTypeEncoder: Encoder[QuoteType] =
    stringNodeEncoder[QuoteType]

  implicit def TargetEncoder: Encoder[Target] =
    Encoder[(String, String)].contramap(unlift(Target.unapply))

  implicit def MathTypeEncoder: Encoder[MathType] =
    stringNodeEncoder[MathType]

  implicit def CitationEncoder: Encoder[Citation] =
    Encoder[(String, List[Inline], List[Inline], CitationMode, Int, Int)].contramap(unlift(Citation.unapply))

  implicit def CitationModeEncoder: Encoder[CitationMode] =
    stringNodeEncoder[CitationMode]
}

trait EncoderHelpers {
  def typedNode[C: Encoder](t: String)(c: C): Json =
    Json.obj("t" -> Json.string(t), "c" -> c.asJson)

  def stringNodeEncoder[A]: Encoder[A] =
    Encoder.instance[A](value => Json.obj("t" -> Json.string(value.toString), "c" -> Json.arr()))

  def unlift[A, B](func: A => Option[B]): A => B =
    (a: A) => func(a).get
}