package spandoc
package transform

import cats.data.State
import cats.instances.list._
import cats.syntax.traverse._
import io.circe._
import io.circe.jawn._
import io.circe.syntax._
import unindent._

class TopDownSpec extends munit.FunSuite {

  test("plain transform") {
    val uppercase = TopDown.inline { case Str(str) =>
      Str(str.toUpperCase)
    }

    val uppercaseHeaders = TopDown.block { case Header(l, a, i) =>
      Header(l, a, i.map(uppercase.apply))
    }

    val actual = uppercaseHeaders(
      Pandoc(
        List(
          Header(1, List(Str("Header"), Space, Str("the"), Space, Str("first"))),
          Para(List(Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
          Header(2, List(Str("Header"), Space, Str("the"), Space, Str("second"))),
          Para(List(Str("Paragraph"), Space, Str("the"), Space, Str("second")))
        )
      )
    )

    val expected = Pandoc(
      List(
        Header(1, List(Str("HEADER"), Space, Str("THE"), Space, Str("FIRST"))),
        Para(List(Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
        Header(2, List(Str("HEADER"), Space, Str("THE"), Space, Str("SECOND"))),
        Para(List(Str("Paragraph"), Space, Str("the"), Space, Str("second")))
      )
    )

    assert(actual == expected)
  }

  test("monadic transform") {
    type IsFirstWord[A] = State[Boolean, A]

    val uppercaseFirstWord = new TopDown[IsFirstWord] {
      val blockTransform: BlockTransform = {
        case Header(level, attr, inlines) =>
          for {
            _ <- State.set(true)
            inlines <- inlines.map(this.apply).sequence
          } yield Header(level, attr, inlines)

        case Para(inlines) =>
          for {
            _ <- State.set(true)
            inlines <- inlines.map(this.apply).sequence
          } yield Para(inlines)
      }

      val inlineTransform: InlineTransform = { case Str(str) =>
        for {
          upper <- State.get
          _ <- State.set(false)
          ans <- State.pure(if (upper) Str(str.toUpperCase) else Str(str))
        } yield ans
      }
    }

    val actual = uppercaseFirstWord(
      Pandoc(
        List(
          Header(1, List(Str("Header"), Space, Str("the"), Space, Str("first"))),
          Para(List(Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
          Header(2, List(Str("Header"), Space, Str("the"), Space, Str("second"))),
          Para(List(Str("Paragraph"), Space, Str("the"), Space, Str("second")))
        )
      )
    ).runA(true).value

    val expected = Pandoc(
      List(
        Header(1, List(Str("HEADER"), Space, Str("the"), Space, Str("first"))),
        Para(List(Str("PARAGRAPH"), Space, Str("the"), Space, Str("first"))),
        Header(2, List(Str("HEADER"), Space, Str("the"), Space, Str("second"))),
        Para(List(Str("PARAGRAPH"), Space, Str("the"), Space, Str("second")))
      )
    )

    assert(actual == expected)
  }
}
