package spandoc

import cats.Monad
import cats.std.list._
import cats.syntax.cartesian._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._

object MonadicTransform {
  def block[F[_]: Monad](blockFunc: PartialFunction[Block, F[Block]]): MonadicTransform[F] =
    new MonadicTransform[F](blockFunc, PartialFunction[Inline, F[Inline]](Monad[F].pure))

  def inline[F[_]: Monad](inlineFunc: PartialFunction[Inline, F[Inline]]): MonadicTransform[F] =
    new MonadicTransform[F](PartialFunction[Block, F[Block]](Monad[F].pure), inlineFunc)
}

class MonadicTransform[F[_]](val blockFunc: PartialFunction[Block, F[Block]], val inlineFunc: PartialFunction[Inline, F[Inline]])(implicit monad: Monad[F]) {
  private def pure[A](value: A): F[A] =
    monad.pure(value)

  def apply(pandoc0: Pandoc): F[Pandoc] =
    pandoc0.blocks.traverse(apply).map(Pandoc(pandoc0.meta, _))

  def apply(block0: Block): F[Block] = {
    val block1: F[Block] = block0 match {
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

    for {
      block1 <- block1
      block2 <- blockFunc.lift(block1).getOrElse(pure(block1))
    } yield block2
  }

  def apply(inline0: Inline): F[Inline] = {
    val inline1: F[Inline] = inline0 match {
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

    for {
      inline1 <- inline1
      inline2 <- inlineFunc.lift(inline1).getOrElse(pure(inline1))
    } yield inline2
  }

  def apply(item: ListItem): F[ListItem] =
    item.blocks.traverse(apply).map(ListItem(_))

  def apply(item: DefinitionItem): F[DefinitionItem] = (
    item.term.traverse(apply) |@|
    item.definitions.traverse(apply)
  ).map(DefinitionItem(_, _))

  def apply(defn: Definition): F[Definition] =
    defn.blocks.traverse(apply).map(Definition(_))

  def apply(row: TableRow): F[TableRow] =
    row.cells.traverse(apply).map(TableRow(_))

  def apply(cell: TableCell): F[TableCell] =
    cell.blocks.traverse(apply).map(TableCell(_))
}