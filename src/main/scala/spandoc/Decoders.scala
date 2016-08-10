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
    typedNode[Block] {
      case "Plain"          => Decoder[List[Inline]].map[Block](Plain.apply)
      case "Para"           => Decoder[List[Inline]].map[Block](Para.apply)
      case "CodeBlock"      => Decoder[(Attr, String)].map[Block](CodeBlock.tupled)
      case "RawBlock"       => Decoder[(String, String)].map[Block](RawBlock.tupled)
      case "BlockQuote"     => Decoder[List[Block]].map[Block](BlockQuote.apply)
      case "OrderedList"    => Decoder[(ListAttributes, List[ListItem])].map[Block](OrderedList.tupled)
      case "BulletList"     => Decoder[List[ListItem]].map[Block](BulletList.apply)
      case "DefinitionList" => ???
      case "Header"         => Decoder[(Int, Attr, List[Inline])].map[Block](Header.tupled)
      case "HorizontalRule" => constant[Block](HorizontalRule)
      case "Table"          => ???
      case "Div"            => Decoder[(Attr, List[Block])].map[Block](Div.tupled)
      case "Null"           => constant[Block](Null)
    }

  implicit def InlineDecoder: Decoder[Inline] =
    typedNode[Inline] {
      case "Str"         => Decoder[String].map[Inline](Str.apply)
      case "Emph"        => Decoder[List[Inline]].map[Inline](Emph.apply)
      case "Strong"      => Decoder[List[Inline]].map[Inline](Strong.apply)
      case "Strikeout"   => Decoder[List[Inline]].map[Inline](Strikeout.apply)
      case "Superscript" => Decoder[List[Inline]].map[Inline](Superscript.apply)
      case "Subscript"   => Decoder[List[Inline]].map[Inline](Subscript.apply)
      case "SmallCaps"   => Decoder[List[Inline]].map[Inline](SmallCaps.apply)
      case "Quoted"      => Decoder[(QuoteType, List[Inline])].map[Block](Quoted.tupled)
      case "Cite"        => ???
      case "Code"        => ???
      case "Space"       => constant[Inline](Space)
      case "SoftBreak"   => constant[Inline](SoftBreak)
      case "LineBreak"   => constant[Inline](LineBreak)
      case "Math"        => ???
      case "RawInline"   => ???
      case "Link"        => ???
      case "Image"       => ???
      case "Note"        => ???
      case "Span"        => ???
    }

  // TODO: Alignment

  implicit def ListAttributesDecoder: Decoder[ListAttributes] =
    Decoder[(Int, ListNumberStyle, ListNumberDelim)].map(ListAttributes.tupled)

  implicit def ListItemDecoder: Decoder[ListItem] =
    Decoder[List[Block]].map(ListItem.apply)

  implicit def ListNumberStyleDecoder: Decoder[ListNumberStyle] =
    typedNode[ListNumberStyle] {
      case "DefaultStyle" => constant[ListNumberStyle](DefaultStyle)
      case "Example"      => constant[ListNumberStyle](Example)
      case "Decimal"      => constant[ListNumberStyle](Decimal)
      case "LowerRoman"   => constant[ListNumberStyle](LowerRoman)
      case "UpperRoman"   => constant[ListNumberStyle](UpperRoman)
      case "LowerAlpha"   => constant[ListNumberStyle](LowerAlpha)
      case "UpperAlpha"   => constant[ListNumberStyle](UpperAlpha)
    }

  implicit def ListNumberDelimDecoder: Decoder[ListNumberDelim] =
    typedNode[ListNumberDelim] {
      case "DefaultDelim" => constant[ListNumberDelim](DefaultDelim)
      case "Period"       => constant[ListNumberDelim](Period)
      case "OneParen"     => constant[ListNumberDelim](OneParen)
      case "TwoParens"    => constant[ListNumberDelim](TwoParens)
    }

  // TODO: DefinitionItem
  // TODO: Definition

  implicit def AttrDecoder: Decoder[Attr] =
    Decoder[(String, List[String], List[(String, String)])].map(Attr.tupled)

  // TODO: TableRow
  // TODO: TableCell

  // TODO: QuoteType

  // TODO: Target

  // TODO: MathType

  // TODO: Citation

  // TODO: CitationMode
}

trait DecoderHelpers {

  def constant[A](value: A): Decoder[A] = new Decoder[A] {
    def apply(cursor: HCursor): Decoder.Result[A] =
      Xor.Right(value)
  }

  def typedNode[A](decoders: PartialFunction[String, Decoder[A]]): Decoder[A] =
    Decoder.instance[A] { cursor =>
      for {
        t <- cursor.downField("t").as[String]
        d <- decoders.lift(t).toRightXor(DecodingFailure(s"Unrecognised type: '$t'", Nil))
        c <- cursor.downField("c").as[A](d)
      } yield c
    }

}