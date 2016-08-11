package spandoc

import cats.data.Xor
import io.circe._
import io.circe.jawn._
import io.circe.syntax._
import org.scalatest._
import unindent._

class TransformSpec extends FreeSpec
  with Matchers
  with TransformSpecHelpers {

  "uppercase" in {
    val uppercase = Transform.inline {
      case Str(str) => Str(str.toUpperCase)
    }

    checkTransform(uppercase) {
      i"""
      [{"unMeta":{}},[
        {"t":"Para","c":[
          {"t":"Str","c":"lorem"},
          {"t":"Space","c":[]},
          {"t":"Str","c":"ipsum"}
        ]}
      ]]
      """
    } {
      i"""
      [{"unMeta":{}},[
        {"t":"Para","c":[
          {"t":"Str","c":"LOREM"},
          {"t":"Space","c":[]},
          {"t":"Str","c":"IPSUM"}
        ]}
      ]]
      """
    }
  }

  "swap nested lists" in {
    val swapLists = Transform.block {
      case OrderedList(_, items) => BulletList(items)
    }

    checkTransform(swapLists) {
      i"""
      [{"unMeta":{}},[
        {"t":"OrderedList","c":[
          [1,{"t":"Decimal","c":[]},{"t":"Period","c":[]}],
          [
            [
              {"t":"Plain","c":[{"t":"Str","c":"item1"}]},
              {"t":"OrderedList","c":[
                [1,{"t":"LowerAlpha","c":[]},{"t":"Period","c":[]}],
                [
                  [{"t":"Plain","c":[{"t":"Str","c":"item1a"}]}],
                  [{"t":"Plain","c":[{"t":"Str","c":"item1b"}]}]
                ]
              ]}
            ],
            [
              {"t":"Plain","c":[{"t":"Str","c":"item2"}]},
              {"t":"OrderedList","c":[
                [1,{"t":"LowerAlpha","c":[]},{"t":"Period","c":[]}],
                [
                  [{"t":"Plain","c":[{"t":"Str","c":"item2a"}]}],
                  [{"t":"Plain","c":[{"t":"Str","c":"item2b"}]}]
                ]
              ]}
            ]
          ]
        ]}
      ]]
      """
    } {
      i"""
      [{"unMeta":{}},[
        {"t":"BulletList","c":[
          [
            {"t":"Plain","c":[{"t":"Str","c":"item1"}]},
            {"t":"BulletList","c":[
              [{"t":"Plain","c":[{"t":"Str","c":"item1a"}]}],
              [{"t":"Plain","c":[{"t":"Str","c":"item1b"}]}]
            ]}
          ],
          [
            {"t":"Plain","c":[{"t":"Str","c":"item2"}]},
            {"t":"BulletList","c":[
              [{"t":"Plain","c":[{"t":"Str","c":"item2a"}]}],
              [{"t":"Plain","c":[{"t":"Str","c":"item2b"}]}]
            ]}
          ]
        ]}
      ]]
      """
    }
  }
}

trait TransformSpecHelpers {
  self: Matchers =>

  def checkTransform(transform: Pandoc => Pandoc)(input: String)(expected: String): Unit = {
    val result = for {
      input    <- parse(input)
      actual   <- transformJson(transform)(input)
      expected <- parse(expected)
    } yield {
      actual.spaces2 should equal(expected.spaces2)
      ()
    }

    result should equal(Xor.Right(()))
  }
}
