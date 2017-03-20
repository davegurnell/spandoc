package spandoc
package transform

import cats.Monad
import cats.instances.list._
import cats.syntax.cartesian._
import cats.syntax.flatMap._
import cats.syntax.functor._
import cats.syntax.traverse._
import scala.language.higherKinds

abstract class Transform[F[_]](implicit monad: Monad[F]) extends (Pandoc => F[Pandoc]) {
  def pure[A](value: A): F[A] =
    monad.pure(value)

  def apply(pandoc0: Pandoc): F[Pandoc] =
    pandoc0.blocks.traverse(apply).map(Pandoc(pandoc0.meta, _))

  def apply(block0: Block): F[Block]

  def apply(inline0: Inline): F[Inline]

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
