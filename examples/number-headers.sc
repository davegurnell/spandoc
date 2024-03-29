#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "0.6.0")

@
import cats.data.State
import spandoc._
import spandoc.transform.TopDown

/* **********************************************
   Note: This has a bunch of bugs in it.
   I haven't gotten around to fixing them yet.
********************************************** */

// State type:

type HeaderLevel = Int
type HeaderNumbers = Map[HeaderLevel, Int]
type HeaderState[A] = State[HeaderNumbers, A]

// Number all headings in a document:
object transform extends TopDown[HeaderState] {
  def blockTransform = { case Header(level, attr, content) =>
    for {
      s <- str(level)
      _ <- inc(level)
      h <- State.pure(Header(level, attr, s +: content))
    } yield h
  }

  def inlineTransform = {}

  // Get the heading number for a particular level:
  def get(level: Int): HeaderState[Int] =
    State.inspect { nums =>
      Console.err.println("get " + level + " " + nums + " => " + nums.getOrElse(level, 1))
      nums.getOrElse(level, 1)
    }

  // Set the heading number for a particular level:
  def set(level: Int, number: Int): HeaderState[Unit] =
    State.modify { nums =>
      Console.err.println("set " + level + " " + number + " " + nums + " => " + (nums + (level -> number)))
      nums + (level                                                                            -> number)
    }

  // Reset counters for levels >= level to 0:
  def reset(level: Int): HeaderState[Unit] =
    State.modify { nums =>
      Console.err.println("reset " + level + " " + nums + " => " + nums.filterNot(_._1 > level))
      nums.filterNot(_._1 > level)
    }

  // Increment the heading number at a level
  // and reset levels greater than level:
  def inc(level: Int): HeaderState[Unit] =
    for {
      n <- get(level)
      _ <- set(level, n + 1)
      _ <- reset(level + 1)
    } yield ()

  // Get a fully formatted heading number
  // for the specified level:
  def str(level: Int): HeaderState[Str] =
    State.inspect { nums =>
      val ans =
        Str(
          (1 to level)
            .map(level => nums.getOrElse(level, 1))
            .mkString("", ".", ". ")
        )
      Console.err.println("string " + ans)
      ans
    }
}

// Run the filter on standard in:
transformStdin(ast => transform(ast).runA(Map.empty).value)
