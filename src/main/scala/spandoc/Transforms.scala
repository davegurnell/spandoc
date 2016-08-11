package spandoc

object Transform {
  def block(blockFunc: PartialFunction[Block, Block]): Transform =
    Transform(blockFunc, PartialFunction[Inline, Inline](identity))

  def inline(inlineFunc: PartialFunction[Inline, Inline]): Transform =
    Transform(PartialFunction[Block, Block](identity), inlineFunc)
}

case class Transform(blockFunc: PartialFunction[Block, Block], inlineFunc: PartialFunction[Inline, Inline]) extends (Pandoc => Pandoc) {
  def apply(pandoc: Pandoc): Pandoc = {
    val Pandoc(meta, blocks) = pandoc
    Pandoc(meta, blocks map apply)
  }

  def apply(block0: Block): Block = {
    val block1: Block = block0 match {
      case Plain(inlines)               => Plain(inlines map apply)
      case Para(inlines)                => Para(inlines map apply)
      case b: CodeBlock                 => b
      case b: RawBlock                  => b
      case BlockQuote(blocks)           => BlockQuote(blocks map apply)
      case OrderedList(attr, items)     => OrderedList(attr, items map apply)
      case BulletList(items)            => BulletList(items map apply)
      case DefinitionList(items)        => DefinitionList(items map apply)
      case Header(level, attr, inlines) => Header(level, attr, inlines map apply)
      case HorizontalRule               => HorizontalRule
      case Table(c, a, w, h, r)         => Table(c map apply, a, w, h map apply, r map apply)
      case Div(attr, blocks)            => Div(attr, blocks map apply)
      case Null                         => Null
    }

    blockFunc.lift(block1).getOrElse(block1)
  }

  def apply(inline0: Inline): Inline = {
    val inline1: Inline = inline0 match {
      case i: Str                   => i
      case Emph(inlines)            => Emph(inlines map apply)
      case Strong(inlines)          => Strong(inlines map apply)
      case Strikeout(inlines)       => Strikeout(inlines map apply)
      case Superscript(inlines)     => Superscript(inlines map apply)
      case Subscript(inlines)       => Subscript(inlines map apply)
      case SmallCaps(inlines)       => SmallCaps(inlines map apply)
      case Quoted(tpe, inlines)     => Quoted(tpe, inlines map apply)
      case Cite(citations, inlines) => Cite(citations, inlines map apply)
      case b: Code                  => b
      case Space                    => Space
      case SoftBreak                => SoftBreak
      case LineBreak                => LineBreak
      case b: Math                  => b
      case b: RawInline             => b
      case Link(inlines, target)    => Link(inlines map apply, target)
      case Image(inlines, target)   => Image(inlines map apply, target)
      case Note(blocks)             => Note(blocks map apply)
      case Span(attr, inlines)      => Span(attr, inlines map apply)
    }

    inlineFunc.lift(inline1).getOrElse(inline1)
  }

  def apply(item: ListItem): ListItem = {
    val ListItem(blocks) = item
    ListItem(blocks map apply)
  }

  def apply(item: DefinitionItem): DefinitionItem = {
    val DefinitionItem(term, defns) = item
    DefinitionItem(term map apply, defns map apply)
  }

  def apply(defn: Definition): Definition = {
    val Definition(blocks) = defn
    Definition(blocks map apply)
  }

  def apply(row: TableRow): TableRow = {
    val TableRow(cells) = row
    TableRow(cells map apply)
  }

  def apply(cell: TableCell): TableCell = {
    val TableCell(blocks) = cell
    TableCell(blocks map apply)
  }
}