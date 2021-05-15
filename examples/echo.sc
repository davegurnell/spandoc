#!/usr/bin/env amm

load.ivy("com.davegurnell" %% "spandoc" % "0.6.0")

@
import spandoc._

transformStdin { pandoc =>
  Console.err.println(pandoc)
  pandoc
}
