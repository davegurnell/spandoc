package spandoc.ast

final case class Pandoc(blocks: Vector[Block], meta: Meta = Meta.empty, apiVersion: PandocApiVersion = Pandoc.defaultVersion)

final case class PandocApiVersion(major: Int, minor: Int)

object Pandoc {
  val defaultVersion: PandocApiVersion =
    PandocApiVersion(1, 20)

  def apply(blocks: Vector[Block]): Pandoc =
    Pandoc(blocks, Meta.empty, defaultVersion)
}
