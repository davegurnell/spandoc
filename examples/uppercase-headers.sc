#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "0.2.1")

@
import spandoc._
import spandoc.transform.TopDown
import cats.data.State
import cats.instances.list._
import cats.syntax.traverse._

type HeaderState[A] = State[Boolean, A]

object filter extends TopDown[HeaderState] {
  def blockTransform = { case Header(level, attr, inlineNodes) =>
    for {
      _ <- State.set(true)
      inlines <- inlineNodes.map(this.apply).sequence
      _ <- State.set(false)
    } yield Header(level, attr, inlines)
  }

  def inlineTransform = { case str @ Str(text) =>
    State.inspect { inHeader =>
      if (inHeader) Str(text.toUpperCase) else str
    }
  }
}

transformStdin(ast => filter(ast).runA(false).value)
