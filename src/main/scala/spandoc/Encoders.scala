package spandoc

import cats.data._
import cats.syntax.all._
import io.circe._
import io.circe.syntax._

trait Encoders extends EncoderHelpers {
  implicit lazy val pandocEncoder: Encoder[Pandoc] =
    Encoder.instance[Pandoc] { pandoc =>
      Json.obj(
        "blocks"             -> pandoc.blocks.asJson,
        "meta"               -> pandoc.meta.asJson,
        "pandoc-api-version" -> pandoc.apiVersion.asJson
      )
    }

  implicit lazy val pandocApiVersionEncoder: Encoder[PandocApiVersion] =
    Encoder.instance[PandocApiVersion](v => (v.major, v.minor).asJson)

  implicit lazy val metaEncoder: Encoder[Meta] =
    Encoder.instance[Meta](meta => meta.data.asJson)

  implicit lazy val metaValueEncoder: Encoder[MetaValue] =
    Encoder.instance[MetaValue] {
      case MetaMap(values)      => typedNode("MetaMap")(values)
      case MetaList(values)     => typedNode("MetaList")(values)
      case MetaBool(value)      => typedNode("MetaBool")(value)
      case MetaString(value)    => typedNode("MetaString")(value)
      case MetaInlines(inlines) => typedNode("MetaInlines")(inlines)
      case MetaBlocks(blocks)   => typedNode("MetaBlocks")(blocks)
    }

  implicit lazy val blockEncoder: Encoder[Block] =
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

  implicit lazy val inlineEncoder: Encoder[Inline] =
    Encoder.instance[Inline] {
      case Str(text)                   => typedNode("Str")(text)
      case Emph(inlines)               => typedNode("Emph")(inlines)
      case Strong(inlines)             => typedNode("Strong")(inlines)
      case Strikeout(inlines)          => typedNode("Strikeout")(inlines)
      case Superscript(inlines)        => typedNode("Superscript")(inlines)
      case Subscript(inlines)          => typedNode("Subscript")(inlines)
      case SmallCaps(inlines)          => typedNode("SmallCaps")(inlines)
      case Quoted(tpe, inlines)        => typedNode("Quoted")((tpe, inlines))
      case Cite(citations, inlines)    => typedNode("Cite")((citations, inlines))
      case Code(attr, text)            => typedNode("Code")((attr, text))
      case Space                       => typedNode("Space")(Json.arr())
      case SoftBreak                   => typedNode("SoftBreak")(Json.arr())
      case LineBreak                   => typedNode("LineBreak")(Json.arr())
      case Math(tpe, text)             => typedNode("Math")((tpe, text))
      case RawInline(format, text)     => typedNode("RawInline")((format, text))
      case Link(attr, inlines, target) => typedNode("Link")((attr, inlines, target))
      case Image(inlines, target)      => typedNode("Image")((inlines, target))
      case Note(blocks)                => typedNode("Note")(blocks)
      case Span(attr, inlines)         => typedNode("Span")((attr, inlines))
    }

  implicit lazy val alignmentEncoder: Encoder[Alignment] =
    stringNodeEncoder[Alignment]

  implicit lazy val listAttributesEncoder: Encoder[ListAttributes] =
    Encoder[(Int, ListNumberStyle, ListNumberDelim)].contramap(unlift(ListAttributes.unapply))

  implicit lazy val listItemEncoder: Encoder[ListItem] =
    Encoder[Vector[Block]].contramap(unlift(ListItem.unapply))

  implicit lazy val listNumberStyleEncoder: Encoder[ListNumberStyle] =
    stringNodeEncoder[ListNumberStyle]

  implicit lazy val listNumberDelimEncoder: Encoder[ListNumberDelim] =
    stringNodeEncoder[ListNumberDelim]

  implicit lazy val definitionItemEncoder: Encoder[DefinitionItem] =
    Encoder[(Vector[Inline], Vector[Definition])].contramap(unlift(DefinitionItem.unapply))

  implicit lazy val definitionEncoder: Encoder[Definition] =
    Encoder[Vector[Block]].contramap(unlift(Definition.unapply))

  implicit lazy val attrEncoder: Encoder[Attr] =
    Encoder[(String, Vector[String], Vector[(String, String)])].contramap(unlift(Attr.unapply))

  implicit lazy val tableRowEncoder: Encoder[TableRow] =
    Encoder[Vector[TableCell]].contramap(unlift(TableRow.unapply))

  implicit lazy val tableCellEncoder: Encoder[TableCell] =
    Encoder[Vector[Block]].contramap(unlift(TableCell.unapply))

  implicit lazy val quoteTypeEncoder: Encoder[QuoteType] =
    stringNodeEncoder[QuoteType]

  implicit lazy val targetEncoder: Encoder[Target] =
    Encoder[(String, String)].contramap(unlift(Target.unapply))

  implicit lazy val mathTypeEncoder: Encoder[MathType] =
    stringNodeEncoder[MathType]

  implicit lazy val citationEncoder: Encoder[Citation] =
    Encoder[(String, Vector[Inline], Vector[Inline], CitationMode, Int, Int)].contramap(unlift(Citation.unapply))

  implicit lazy val citationModeEncoder: Encoder[CitationMode] =
    stringNodeEncoder[CitationMode]
}

trait EncoderHelpers {
  def typedNode[C: Encoder](t: String)(c: C): Json =
    Json.obj("t" -> Json.fromString(t), "c" -> c.asJson)

  def stringNodeEncoder[A]: Encoder[A] =
    Encoder.instance[A](value => Json.obj("t" -> Json.fromString(value.toString), "c" -> Json.arr()))

  def unlift[A, B](func: A => Option[B]): A => B =
    (a: A) => func(a).get
}
