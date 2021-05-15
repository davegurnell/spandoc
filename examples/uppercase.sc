#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "0.6.0")

@
import spandoc._
import spandoc.transform.TopDown

object transform extends TopDown[Id] {
  def blockTransform = {}

  def inlineTransform = { case Str(text) =>
    Str(text.toUpperCase)
  }
}

transformStdin(transform)
