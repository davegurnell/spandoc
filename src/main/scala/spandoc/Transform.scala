package spandoc

import cats.Id

object Transform {
  def block(blockFunc: PartialFunction[Block, Block]): Transform =
    new Transform(blockFunc, PartialFunction[Inline, Inline](identity))

  def inline(inlineFunc: PartialFunction[Inline, Inline]): Transform =
    new Transform(PartialFunction[Block, Block](identity), inlineFunc)
}

class Transform(blockFunc: PartialFunction[Block, Block], inlineFunc: PartialFunction[Inline, Inline])
  extends MonadicTransform[Id](blockFunc, inlineFunc)
