package spandoc
package transform

import cats.Id
import cats.data.State
import cats.instances.list._
import cats.syntax.traverse._
import io.circe._
import io.circe.jawn._
import io.circe.syntax._
import unindent._

class BottomUpSpec extends munit.FunSuite {
  test("plain transform") {
    val lolcatify = new BottomUp[Id] {
      val blockTransform: BlockTransform = { case Para(blocks) =>
        Para(blocks ++ List(Space, Str("!!1!")))
      }

      val inlineTransform: InlineTransform = {
        case Str("the") => Str("TEH")
        case Str(text)  => Str(text.toUpperCase)
      }
    }

    val actual = lolcatify(
      Pandoc(
        Vector(
          Header(1, Vector(Str("Header"), Space, Str("the"), Space, Str("first"))),
          Para(Vector(Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
          Header(2, Vector(Str("Header"), Space, Str("the"), Space, Str("second"))),
          Para(Vector(Str("Paragraph"), Space, Str("the"), Space, Str("second")))
        )
      )
    )

    val expected = Pandoc(
      Vector(
        Header(1, Vector(Str("HEADER"), Space, Str("TEH"), Space, Str("FIRST"))),
        Para(Vector(Str("PARAGRAPH"), Space, Str("TEH"), Space, Str("FIRST"), Space, Str("!!1!"))),
        Header(2, Vector(Str("HEADER"), Space, Str("TEH"), Space, Str("SECOND"))),
        Para(Vector(Str("PARAGRAPH"), Space, Str("TEH"), Space, Str("SECOND"), Space, Str("!!1!")))
      )
    )

    assert(actual == expected)
  }

  test("monadic transform") {
    type ParaNumber[A] = State[Int, A]

    val nextNumber: ParaNumber[Inline] =
      for {
        _ <- State.modify[Int](_ + 1)
        n <- State.get[Int]
      } yield Str(n.toString)

    val numberParas = BottomUp.blockM[ParaNumber] {
      case h @ Header(_, _, inl) => nextNumber.map(num => h.copy(inlines = Vector(num, Space) ++ inl))
      case p @ Para(inl)         => nextNumber.map(num => p.copy(inlines = Vector(num, Space) ++ inl))
    }

    val actual = numberParas(
      Pandoc(
        Vector(
          Header(1, Vector(Str("Header"), Space, Str("the"), Space, Str("first"))),
          Para(Vector(Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
          Header(2, Vector(Str("Header"), Space, Str("the"), Space, Str("second"))),
          Para(Vector(Str("Paragraph"), Space, Str("the"), Space, Str("second")))
        )
      )
    ).runA(0).value

    val expected = Pandoc(
      Vector(
        Header(1, Vector(Str("1"), Space, Str("Header"), Space, Str("the"), Space, Str("first"))),
        Para(Vector(Str("2"), Space, Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
        Header(2, Vector(Str("3"), Space, Str("Header"), Space, Str("the"), Space, Str("second"))),
        Para(Vector(Str("4"), Space, Str("Paragraph"), Space, Str("the"), Space, Str("second")))
      )
    )

    assert(actual == expected)
  }
}
