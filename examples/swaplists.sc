#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "0.6.0")

@
import spandoc._
import spandoc.transform.TopDown

val filter = TopDown.block {
  case OrderedList(_, items) => BulletList(items)
  case BulletList(items)     => OrderedList(ListAttributes(1, Decimal, Period), items)
}

transformStdin(filter)
