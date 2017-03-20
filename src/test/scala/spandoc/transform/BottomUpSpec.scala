package spandoc
package transform

import cats.Id
import cats.data.State
import cats.instances.list._
import cats.syntax.traverse._
import io.circe._
import io.circe.jawn._
import io.circe.syntax._
import org.scalatest._
import unindent._

class BottomUpSpec extends FreeSpec with Matchers {
  "plain transform" in {
    val lolcatify = new BottomUp[Id] {
      val blockTransform: BlockTransform = {
        case Para(blocks) => Para(blocks ++ List(Space, Str("!!1!")))
      }

      val inlineTransform: InlineTransform = {
        case Str("the") => Str("TEH")
        case Str(text)  => Str(text.toUpperCase)
      }
    }

    val actual = lolcatify(Pandoc(List(
      Header(1, List(Str("Header"),    Space, Str("the"), Space, Str("first"))),
      Para(     List(Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
      Header(2, List(Str("Header"),    Space, Str("the"), Space, Str("second"))),
      Para(     List(Str("Paragraph"), Space, Str("the"), Space, Str("second")))
    )))

    val expected = Pandoc(List(
      Header(1, List(Str("HEADER"),    Space, Str("TEH"), Space, Str("FIRST"))),
      Para(     List(Str("PARAGRAPH"), Space, Str("TEH"), Space, Str("FIRST"),  Space, Str("!!1!"))),
      Header(2, List(Str("HEADER"),    Space, Str("TEH"), Space, Str("SECOND"))),
      Para(     List(Str("PARAGRAPH"), Space, Str("TEH"), Space, Str("SECOND"), Space, Str("!!1!")))
    ))

    actual should equal(expected)
  }

  "monadic transform" in {
    type ParaNumber[A] = State[Int, A]

    val nextNumber: ParaNumber[Inline] =
      for {
        _ <- State.modify[Int](_ + 1)
        n <- State.get[Int]
      } yield Str(n.toString)

    val numberParas = BottomUp.blockM[ParaNumber] {
      case h @ Header(_, _, inl) => nextNumber.map(num => h.copy(inlines = List(num, Space) ++ inl))
      case p @ Para(inl)         => nextNumber.map(num => p.copy(inlines = List(num, Space) ++ inl))
    }

    val actual = numberParas(Pandoc(List(
      Header(1, List(Str("Header"),    Space, Str("the"), Space, Str("first"))),
      Para(     List(Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
      Header(2, List(Str("Header"),    Space, Str("the"), Space, Str("second"))),
      Para(     List(Str("Paragraph"), Space, Str("the"), Space, Str("second")))
    ))).runA(0).value

    val expected = Pandoc(List(
      Header(1, List(Str("1"), Space, Str("Header"),    Space, Str("the"), Space, Str("first"))),
      Para(     List(Str("2"), Space, Str("Paragraph"), Space, Str("the"), Space, Str("first"))),
      Header(2, List(Str("3"), Space, Str("Header"),    Space, Str("the"), Space, Str("second"))),
      Para(     List(Str("4"), Space, Str("Paragraph"), Space, Str("the"), Space, Str("second")))
    ))

    actual should equal(expected)
  }
}
