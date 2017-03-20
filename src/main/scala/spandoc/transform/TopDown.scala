package spandoc
package transform

import cats.{Id, Monad}
import cats.instances.list._
import cats.syntax.cartesian._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import scala.language.higherKinds

object TopDown {
  def block(func: PartialFunction[Block, Block]): TopDown[Id] =
    new TopDown[Id] {
      val blockTransform  = func
      val inlineTransform = PartialFunction.empty
    }

  def inline(func: PartialFunction[Inline, Inline]): TopDown[Id] =
    new TopDown[Id] {
      val blockTransform  = PartialFunction.empty
      val inlineTransform = func
    }

  def blockM[F[_]: Monad](func: PartialFunction[Block, F[Block]]): TopDown[F] =
    new TopDown[F] {
      val blockTransform  = func
      val inlineTransform = PartialFunction.empty
    }

  def inlineM[F[_]: Monad](func: PartialFunction[Inline, F[Inline]]): TopDown[F] =
    new TopDown[F] {
      val blockTransform  = PartialFunction.empty
      val inlineTransform = func
    }
}

abstract class TopDown[F[_]](implicit monad: Monad[F]) extends Transform[F] {
  type BlockTransform  = PartialFunction[Block,  F[Block]]
  type InlineTransform = PartialFunction[Inline, F[Inline]]

  def blockTransform  : BlockTransform
  def inlineTransform : InlineTransform

  object BlockMatches {
    def unapply(block: Block): Option[F[Block]] =
      blockTransform.lift(block)
  }

  object InlineMatches {
    def unapply(inline: Inline): Option[F[Inline]] =
      inlineTransform.lift(inline)
  }

  def apply(block0: Block): F[Block] =
    block0 match {
      case BlockMatches(block)          => block
      case Plain(inlines)               => inlines.traverse(apply).map(Plain(_))
      case Para(inlines)                => inlines.traverse(apply).map(Para(_))
      case b: CodeBlock                 => pure(b)
      case b: RawBlock                  => pure(b)
      case BlockQuote(blocks)           => blocks.traverse(apply).map(BlockQuote(_))
      case OrderedList(attr, items)     => items.traverse(apply).map(OrderedList(attr, _))
      case BulletList(items)            => items.traverse(apply).map(BulletList(_))
      case DefinitionList(items)        => items.traverse(apply).map(DefinitionList(_))
      case Header(level, attr, inlines) => inlines.traverse(apply).map(Header(level, attr, _))
      case HorizontalRule               => pure(HorizontalRule)
      case Table(c, a, w, h, r)         => (
                                             c.traverse(apply) |@|
                                             h.traverse(apply) |@|
                                             r.traverse(apply)
                                           ).map(Table(_, a, w, _, _))
      case Div(attr, blocks)            => blocks.traverse(apply).map(Div(attr, _))
      case Null                         => pure(Null)
    }

  def apply(inline0: Inline): F[Inline] =
    inline0 match {
      case InlineMatches(inline)        => inline
      case i: Str                       => pure(i)
      case Emph(inlines)                => inlines.traverse(apply).map(Emph(_))
      case Strong(inlines)              => inlines.traverse(apply).map(Strong(_))
      case Strikeout(inlines)           => inlines.traverse(apply).map(Strikeout(_))
      case Superscript(inlines)         => inlines.traverse(apply).map(Superscript(_))
      case Subscript(inlines)           => inlines.traverse(apply).map(Subscript(_))
      case SmallCaps(inlines)           => inlines.traverse(apply).map(SmallCaps(_))
      case Quoted(tpe, inlines)         => inlines.traverse(apply).map(Quoted(tpe, _))
      case Cite(citations, inlines)     => inlines.traverse(apply).map(Cite(citations, _))
      case b: Code                      => pure(b)
      case Space                        => pure(Space)
      case SoftBreak                    => pure(SoftBreak)
      case LineBreak                    => pure(LineBreak)
      case b: Math                      => pure(b)
      case b: RawInline                 => pure(b)
      case Link(inlines, target)        => inlines.traverse(apply).map(Link(_, target))
      case Image(inlines, target)       => inlines.traverse(apply).map(Image(_, target))
      case Note(blocks)                 => blocks.traverse(apply).map(Note(_))
      case Span(attr, inlines)          => inlines.traverse(apply).map(Span(attr, _))
    }
}