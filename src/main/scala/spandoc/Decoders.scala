package spandoc

import cats.data._
import cats.implicits._
import io.circe._
import io.circe.syntax._

trait Decoders extends DecoderHelpers {
  implicit lazy val pandocDecoder: Decoder[Pandoc] =
    Decoder.instance[Pandoc] { cursor =>
      (
        cursor.downField("blocks").as[Vector[Block]],
        cursor.downField("meta").as[Meta],
        cursor.downField("pandoc-api-version").as[PandocApiVersion]
      ).mapN(Pandoc.apply)
    }

  implicit lazy val pandocApiVersion: Decoder[PandocApiVersion] =
    Decoder[(Int, Int)].map(PandocApiVersion.tupled)

  implicit lazy val metaDecoder: Decoder[Meta] =
    Decoder[Map[String, MetaValue]].map(Meta.apply)

  implicit lazy val metaValueDecoder: Decoder[MetaValue] =
    nodeDecoder[MetaValue] {
      case "MetaMap"     => nodeContent[Map[String, MetaValue]].map[MetaValue](MetaMap.apply)
      case "MetaList"    => nodeContent[Vector[MetaValue]].map[MetaValue](MetaList.apply)
      case "MetaBool"    => nodeContent[Boolean].map[MetaValue](MetaBool.apply)
      case "MetaString"  => nodeContent[String].map[MetaValue](MetaString.apply)
      case "MetaInlines" => nodeContent[Vector[Inline]].map[MetaValue](MetaInlines.apply)
      case "MetaBlocks"  => nodeContent[Vector[Block]].map[MetaValue](MetaBlocks.apply)
    }

  implicit lazy val blockDecoder: Decoder[Block] =
    nodeDecoder[Block] {
      case "Plain"          => nodeContent[Vector[Inline]].map[Block](Plain.apply)
      case "Para"           => nodeContent[Vector[Inline]].map[Block](Para.apply)
      case "CodeBlock"      => nodeContent[(Attr, String)].map[Block](CodeBlock.tupled)
      case "RawBlock"       => nodeContent[(String, String)].map[Block](RawBlock.tupled)
      case "BlockQuote"     => nodeContent[Vector[Block]].map[Block](BlockQuote.apply)
      case "OrderedList"    => nodeContent[(ListAttributes, Vector[ListItem])].map[Block](OrderedList.tupled)
      case "BulletList"     => nodeContent[Vector[ListItem]].map[Block](BulletList.apply)
      case "DefinitionList" => nodeContent[Vector[DefinitionItem]].map[Block](DefinitionList.apply)
      case "Header"         => nodeContent[(Int, Attr, Vector[Inline])].map[Block] { case (i, a, l) => Header(i, a, l) }
      case "HorizontalRule" => constant[Block](HorizontalRule)
      case "Table"          => nodeContent[(Vector[Inline], Vector[Alignment], Vector[Double], Vector[TableCell], Vector[TableRow])].map[Block](Table.tupled)
      case "Div"            => nodeContent[(Attr, Vector[Block])].map[Block](Div.tupled)
      case "Null"           => constant[Block](Null)
    }

  implicit lazy val inlineDecoder: Decoder[Inline] =
    nodeDecoder[Inline] {
      case "Str"         => nodeContent[String].map[Inline](Str.apply)
      case "Emph"        => nodeContent[Vector[Inline]].map[Inline](Emph.apply)
      case "Strong"      => nodeContent[Vector[Inline]].map[Inline](Strong.apply)
      case "Strikeout"   => nodeContent[Vector[Inline]].map[Inline](Strikeout.apply)
      case "Superscript" => nodeContent[Vector[Inline]].map[Inline](Superscript.apply)
      case "Subscript"   => nodeContent[Vector[Inline]].map[Inline](Subscript.apply)
      case "SmallCaps"   => nodeContent[Vector[Inline]].map[Inline](SmallCaps.apply)
      case "Quoted"      => nodeContent[(QuoteType, Vector[Inline])].map[Inline](Quoted.tupled)
      case "Cite"        => nodeContent[(Vector[Citation], Vector[Inline])].map[Inline](Cite.tupled)
      case "Code"        => nodeContent[(Attr, String)].map[Inline](Code.tupled)
      case "Space"       => constant[Inline](Space)
      case "SoftBreak"   => constant[Inline](SoftBreak)
      case "LineBreak"   => constant[Inline](LineBreak)
      case "Math"        => nodeContent[(MathType, String)].map[Inline](Math.tupled)
      case "RawInline"   => nodeContent[(String, String)].map[Inline](RawInline.tupled)
      case "Link"        => nodeContent[(Attr, Vector[Inline], Target)].map[Inline](Link.tupled)
      case "Image"       => nodeContent[(Vector[Inline], Target)].map[Inline](Image.tupled)
      case "Note"        => nodeContent[Vector[Block]].map[Inline](Note.apply)
      case "Span"        => nodeContent[(Attr, Vector[Inline])].map[Inline](Span.tupled)
    }

  implicit lazy val alignmentDecoder: Decoder[Alignment] =
    nodeDecoder[Alignment] {
      case "AlignLeft"    => constant[Alignment](AlignLeft)
      case "AlignRight"   => constant[Alignment](AlignRight)
      case "AlignCenter"  => constant[Alignment](AlignCenter)
      case "AlignDefault" => constant[Alignment](AlignDefault)
    }

  implicit lazy val listAttributesDecoder: Decoder[ListAttributes] =
    Decoder[(Int, ListNumberStyle, ListNumberDelim)].map(ListAttributes.tupled)

  implicit lazy val listItemDecoder: Decoder[ListItem] =
    Decoder[Vector[Block]].map(ListItem.apply)

  implicit lazy val listNumberStyleDecoder: Decoder[ListNumberStyle] =
    nodeDecoder[ListNumberStyle] {
      case "DefaultStyle" => constant[ListNumberStyle](DefaultStyle)
      case "Example"      => constant[ListNumberStyle](Example)
      case "Decimal"      => constant[ListNumberStyle](Decimal)
      case "LowerRoman"   => constant[ListNumberStyle](LowerRoman)
      case "UpperRoman"   => constant[ListNumberStyle](UpperRoman)
      case "LowerAlpha"   => constant[ListNumberStyle](LowerAlpha)
      case "UpperAlpha"   => constant[ListNumberStyle](UpperAlpha)
    }

  implicit lazy val listNumberDelimDecoder: Decoder[ListNumberDelim] =
    nodeDecoder[ListNumberDelim] {
      case "DefaultDelim" => constant[ListNumberDelim](DefaultDelim)
      case "Period"       => constant[ListNumberDelim](Period)
      case "OneParen"     => constant[ListNumberDelim](OneParen)
      case "TwoParens"    => constant[ListNumberDelim](TwoParens)
    }

  implicit lazy val definitionItemDecoder: Decoder[DefinitionItem] =
    Decoder[(Vector[Inline], Vector[Definition])].map(DefinitionItem.tupled)

  implicit lazy val definitionDecoder: Decoder[Definition] =
    Decoder[Vector[Block]].map(Definition.apply)

  implicit lazy val attrDecoder: Decoder[Attr] =
    Decoder[(String, Vector[String], Vector[(String, String)])].map { case (i, c, a) => Attr(i, c, a) }

  implicit lazy val tableRowDecoder: Decoder[TableRow] =
    Decoder[Vector[TableCell]].map(TableRow.apply)

  implicit lazy val tableCellDecoder: Decoder[TableCell] =
    Decoder[Vector[Block]].map(TableCell.apply)

  implicit lazy val quoteTypeDecoder: Decoder[QuoteType] =
    nodeDecoder[QuoteType] {
      case "SingleQuote" => constant[QuoteType](SingleQuote)
      case "DoubleQuote" => constant[QuoteType](DoubleQuote)
    }

  implicit lazy val targetDecoder: Decoder[Target] =
    Decoder[(String, String)].map(Target.tupled)

  implicit lazy val mathTypeDecoder: Decoder[MathType] =
    nodeDecoder[MathType] {
      case "DisplayMath" => constant[MathType](DisplayMath)
      case "InlineMath"  => constant[MathType](InlineMath)
    }

  implicit lazy val citationDecoder: Decoder[Citation] =
    Decoder[(String, Vector[Inline], Vector[Inline], CitationMode, Int, Int)].map(Citation.tupled)

  implicit lazy val citationModeDecoder: Decoder[CitationMode] =
    nodeDecoder[CitationMode] {
      case "Constructors"   => constant[CitationMode](Constructors)
      case "AuthorInText"   => constant[CitationMode](AuthorInText)
      case "SuppressAuthor" => constant[CitationMode](SuppressAuthor)
      case "NormalCitation" => constant[CitationMode](NormalCitation)
    }
}

trait DecoderHelpers {
  def constant[A](value: A): Decoder[A] = new Decoder[A] {
    def apply(cursor: HCursor): Decoder.Result[A] =
      Right(value)
  }

  def nodeDecoder[A](decoders: PartialFunction[String, Decoder[A]]): Decoder[A] =
    Decoder.instance[A] { cursor =>
      for {
        t <- cursor.downField("t").as[String]
        d <- decoders.lift(t).toRight(DecodingFailure(s"Unrecognised type: '$t'", Nil))
        c <- cursor.as[A](d)
      } yield c
    }

  def nodeContent[A: Decoder]: Decoder[A] =
    Decoder[A].at("c")
}
