package spandoc

import cats.data._
import cats.syntax.all._
import io.circe._
import io.circe.syntax._

trait Decoders extends DecoderHelpers {
  // TODO: Pandoc
  // TODO: Meta
  // TODO: MetaValue

  implicit def BlockDecoder: Decoder[Block] =
    nodeDecoder[Block] {
      case "Plain"          => Decoder[List[Inline]].map[Block](Plain.apply)
      case "Para"           => Decoder[List[Inline]].map[Block](Para.apply)
      case "CodeBlock"      => Decoder[(Attr, String)].map[Block](CodeBlock.tupled)
      case "RawBlock"       => Decoder[(String, String)].map[Block](RawBlock.tupled)
      case "BlockQuote"     => Decoder[List[Block]].map[Block](BlockQuote.apply)
      case "OrderedList"    => Decoder[(ListAttributes, List[ListItem])].map[Block](OrderedList.tupled)
      case "BulletList"     => Decoder[List[ListItem]].map[Block](BulletList.apply)
      case "DefinitionList" => Decoder[List[DefinitionItem]].map[Block](DefinitionList.apply)
      case "Header"         => Decoder[(Int, Attr, List[Inline])].map[Block](Header.tupled)
      case "HorizontalRule" => constant[Block](HorizontalRule)
      case "Table"          => Decoder[(List[Inline], List[Alignment], List[Double], List[TableCell], List[TableRow])].map[Block](Table.tupled)
      case "Div"            => Decoder[(Attr, List[Block])].map[Block](Div.tupled)
      case "Null"           => constant[Block](Null)
    }

  implicit def InlineDecoder: Decoder[Inline] =
    nodeDecoder[Inline] {
      case "Str"         => Decoder[String].map[Inline](Str.apply)
      case "Emph"        => Decoder[List[Inline]].map[Inline](Emph.apply)
      case "Strong"      => Decoder[List[Inline]].map[Inline](Strong.apply)
      case "Strikeout"   => Decoder[List[Inline]].map[Inline](Strikeout.apply)
      case "Superscript" => Decoder[List[Inline]].map[Inline](Superscript.apply)
      case "Subscript"   => Decoder[List[Inline]].map[Inline](Subscript.apply)
      case "SmallCaps"   => Decoder[List[Inline]].map[Inline](SmallCaps.apply)
      case "Quoted"      => Decoder[(QuoteType, List[Inline])].map[Inline](Quoted.tupled)
      case "Cite"        => Decoder[(List[Citation], List[Inline])].map[Inline](Cite.tupled)
      case "Code"        => Decoder[(Attr, String)].map[Inline](Code.tupled)
      case "Space"       => constant[Inline](Space)
      case "SoftBreak"   => constant[Inline](SoftBreak)
      case "LineBreak"   => constant[Inline](LineBreak)
      case "Math"        => Decoder[(MathType, String)].map[Inline](Math.tupled)
      case "RawInline"   => Decoder[(String, String)].map[Inline](RawInline.tupled)
      case "Link"        => Decoder[(List[Inline], Target)].map[Inline](Link.tupled)
      case "Image"       => Decoder[(List[Inline], Target)].map[Inline](Image.tupled)
      case "Note"        => Decoder[List[Block]].map[Inline](Note.apply)
      case "Span"        => Decoder[(Attr, List[Inline])].map[Inline](Span.tupled)
    }

  implicit def AlignmentDecoder: Decoder[Alignment] =
    nodeDecoder[Alignment] {
      case "AlignLeft"    => constant[Alignment](AlignLeft)
      case "AlignRight"   => constant[Alignment](AlignRight)
      case "AlignCenter"  => constant[Alignment](AlignCenter)
      case "AlignDefault" => constant[Alignment](AlignDefault)
    }

  implicit def ListAttributesDecoder: Decoder[ListAttributes] =
    Decoder[(Int, ListNumberStyle, ListNumberDelim)].map(ListAttributes.tupled)

  implicit def ListItemDecoder: Decoder[ListItem] =
    Decoder[List[Block]].map(ListItem.apply)

  implicit def ListNumberStyleDecoder: Decoder[ListNumberStyle] =
    nodeDecoder[ListNumberStyle] {
      case "DefaultStyle" => constant[ListNumberStyle](DefaultStyle)
      case "Example"      => constant[ListNumberStyle](Example)
      case "Decimal"      => constant[ListNumberStyle](Decimal)
      case "LowerRoman"   => constant[ListNumberStyle](LowerRoman)
      case "UpperRoman"   => constant[ListNumberStyle](UpperRoman)
      case "LowerAlpha"   => constant[ListNumberStyle](LowerAlpha)
      case "UpperAlpha"   => constant[ListNumberStyle](UpperAlpha)
    }

  implicit def ListNumberDelimDecoder: Decoder[ListNumberDelim] =
    nodeDecoder[ListNumberDelim] {
      case "DefaultDelim" => constant[ListNumberDelim](DefaultDelim)
      case "Period"       => constant[ListNumberDelim](Period)
      case "OneParen"     => constant[ListNumberDelim](OneParen)
      case "TwoParens"    => constant[ListNumberDelim](TwoParens)
    }

  implicit def DefinitionItemDecoder: Decoder[DefinitionItem] =
    Decoder[(List[Inline], List[Definition])].map(DefinitionItem.tupled)

  implicit def DefinitionDecoder: Decoder[Definition] =
    Decoder[List[Block]].map(Definition.apply)

  implicit def AttrDecoder: Decoder[Attr] =
    Decoder[(String, List[String], List[(String, String)])].map(Attr.tupled)

  implicit def TableRowDecoder: Decoder[TableRow] =
    Decoder[List[TableCell]].map(TableRow.apply)

  implicit def TableCellDecoder: Decoder[TableCell] =
    Decoder[List[Block]].map(TableCell.apply)

  implicit def QuoteTypeDecoder: Decoder[QuoteType] =
    nodeDecoder[QuoteType] {
      case "SingleQuote" => constant[QuoteType](SingleQuote)
      case "DoubleQuote" => constant[QuoteType](DoubleQuote)
    }

  implicit def TargetDecoder: Decoder[Target] =
    Decoder[(String, String)].map(Target.tupled)

  implicit def MathTypeDecoder: Decoder[MathType] =
    nodeDecoder[MathType] {
      case "DisplayMath" => constant[MathType](DisplayMath)
      case "InlineMath"  => constant[MathType](InlineMath)
    }

  implicit def CitationDecoder: Decoder[Citation] =
    Decoder[(String, List[Inline], List[Inline], CitationMode, Int, Int)].map(Citation.tupled)

  implicit def CitationModeDecoder: Decoder[CitationMode] =
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
      Xor.Right(value)
  }

  def nodeDecoder[A](decoders: PartialFunction[String, Decoder[A]]): Decoder[A] =
    Decoder.instance[A] { cursor =>
      for {
        t <- cursor.downField("t").as[String]
        d <- decoders.lift(t).toRightXor(DecodingFailure(s"Unrecognised type: '$t'", Nil))
        c <- cursor.downField("c").as[A](d)
      } yield c
    }
}