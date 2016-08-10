package spandoc

import cats.data.Xor
import io.circe._
import io.circe.jawn._
import io.circe.syntax._
import org.scalatest._
import unindent._

class JsonSpec extends FreeSpec with Matchers with RoundtripHelpers {
  import json._

  "paragraph" in roundtrip[Block] {
    i"""
    {"t":"Para", "c":[
      {"t":"Str","c":"lorem"},
      {"t":"Space","c":[]},
      {"t":"Str","c":"ipsum"}
    ]}
    """
  } {
    Para(List(Str("lorem"), Space, Str("ipsum")))
  }

  "heading" in roundtrip[Block] {
    i"""
    {"t": "Header", "c": [
      1,
      ["heading",[],[]],
      [{"t": "Str", "c": "heading 1"}]
    ]}
    """
  } {
    Header(1, Attr("heading", Nil, Nil), List(Str("heading 1")))
  }

  "bullet list" in roundtrip[Block] {
    i"""
    {"t": "BulletList", "c": [
      [{"t": "Plain", "c": [{"t": "Str", "c": "item1"}]}],
      [{"t": "Plain", "c": [{"t": "Str", "c": "item2"}]}],
      [{"t": "Plain", "c": [{"t": "Str", "c": "item3"}]}]
    ]}
    """
  } {
    BulletList(List(
      ListItem(List(Plain(List(Str("item1"))))),
      ListItem(List(Plain(List(Str("item2"))))),
      ListItem(List(Plain(List(Str("item3")))))
    ))
  }

  "nested bullet lists" in roundtrip[Block] {
    i"""
    {"t": "BulletList", "c": [
      [
        {"t": "Plain", "c": [{"t": "Str", "c": "item1"}]},
        {"t": "BulletList", "c": [
          [{"t": "Plain", "c": [{"t": "Str", "c": "item1a"}]}],
          [{"t": "Plain", "c": [{"t": "Str", "c": "item1b"}]}]
        ]}
      ],
      [
        {"t": "Plain", "c": [{"t": "Str", "c": "item2"}]},
        {"t": "BulletList", "c": [
          [{"t": "Plain", "c": [{"t": "Str", "c": "item2a"}]}],
          [{"t": "Plain", "c": [{"t": "Str", "c": "item2b"}]}]
        ]}
      ]
    ]}
    """
  } {
    BulletList(List(
      ListItem(List(
        Plain(List(Str("item1"))),
        BulletList(List(
          ListItem(List(Plain(List(Str("item1a"))))),
          ListItem(List(Plain(List(Str("item1b")))))
        ))
      )),
      ListItem(List(
        Plain(List(Str("item2"))),
        BulletList(List(
          ListItem(List(Plain(List(Str("item2a"))))),
          ListItem(List(Plain(List(Str("item2b")))))
        ))
      ))
    ))
  }

  "ordered list" in roundtrip[Block] {
    i"""
    {"t":"OrderedList", "c":[
      [1,{"t":"Decimal","c":[]},{"t":"Period","c":[]}],
      [
        [{"t":"Plain","c":[{"t":"Str","c":"item1"}]}],
        [{"t":"Plain","c":[{"t":"Str","c":"item2"}]}],
        [{"t":"Plain","c":[{"t":"Str","c":"item3"}]}]
      ]
    ]}
    """
  } {
    OrderedList(
      ListAttributes(1, Decimal, Period),
      List(
        ListItem(List(Plain(List(Str("item1"))))),
        ListItem(List(Plain(List(Str("item2"))))),
        ListItem(List(Plain(List(Str("item3")))))
      )
    )
  }

  "nested ordered lists" in roundtrip[Block] {
    i"""
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
    """
  } {
    OrderedList(
      ListAttributes(1, Decimal, Period),
      List(
        ListItem(List(
          Plain(List(Str("item1"))),
          OrderedList(
            ListAttributes(1, LowerAlpha, Period),
            List(
              ListItem(List(Plain(List(Str("item1a"))))),
              ListItem(List(Plain(List(Str("item1b")))))
            )
          )
        )),
        ListItem(List(
          Plain(List(Str("item2"))),
          OrderedList(
            ListAttributes(1, LowerAlpha, Period),
            List(
              ListItem(List(Plain(List(Str("item2a"))))),
              ListItem(List(Plain(List(Str("item2b")))))
            )
          )
        ))
      )
    )
  }
}

trait RoundtripHelpers {
  self: Matchers =>

  def roundtrip[A: Decoder](json: String)(data: A): Unit = {
    parse(json) match {
      case Xor.Right(json) =>
        Decoder[A].apply(json.hcursor) should equal(Xor.Right(data))

      case Xor.Left(error) =>
        fail("Could not parse JSON: " + error)
    }
  }
}
