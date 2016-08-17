package spandoc

import cats.data.Xor
import io.circe._
import io.circe.jawn._
import io.circe.syntax._
import org.scalatest._
import unindent._

class JsonSpec extends FreeSpec
  with Matchers
  with Encoders
  with Decoders
  with JsonSpecHelpers {

  "pandoc" in roundtrip[Pandoc] {
    i"""
    [{"unMeta":{}},[]]
    """
  } {
    Pandoc(Meta(Map.empty), Nil)
  }

  "meta" in roundtrip[Meta] {
    i"""
    {"unMeta":{
      "a":{"t":"MetaString","c":"1"},
      "b":{"t":"MetaInlines","c":[
        {"t":"Str","c":"foo"}
      ]},
      "c":{"t":"MetaList","c":[
        {"t":"MetaString","c":"1"},
        {"t":"MetaString","c":"2"},
        {"t":"MetaString","c":"3"}
      ]},
      "d":{"t":"MetaList","c":[
        {"t":"MetaInlines","c":[{"t":"Str","c":"e"}]},
        {"t":"MetaInlines","c":[{"t":"Str","c":"f"}]}
      ]},
      "g":{"t":"MetaBool","c":true},
      "h":{"t":"MetaBlocks","c":[
        {"t":"Para","c":[
          {"t":"Str","c":"lorem"},
          {"t":"Space","c":[]},
          {"t":"Str","c":"ipsum"}
        ]}
      ]}
    }}
    """
  } {
    Meta(Map(
      "a" -> MetaString("1"),
      "b" -> MetaInlines(List(Str("foo"))),
      "c" -> MetaList(List(
               MetaString("1"),
               MetaString("2"),
               MetaString("3")
             )),
      "d" -> MetaList(List(
               MetaInlines(List(Str("e"))),
               MetaInlines(List(Str("f")))
             )),
      "g" -> MetaBool(true),
      "h" -> MetaBlocks(List(
               Para(List(Str("lorem"), Space, Str("ipsum")))
             ))
    ))
  }

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

  "code block" in roundtrip[Block] {
    i"""
    {"t":"CodeBlock","c":[["",["scala"],[]],"1 + 1"]}
    """
  } {
    CodeBlock(Attr("", List("scala"), Nil), "1 + 1")
  }

  "raw block" in roundtrip[Block] {
    i"""
    {"t":"RawBlock","c":["latex", "foo"]}
    """
  } {
    RawBlock("latex", "foo")
  }

  "inline code" in roundtrip[Block] {
    i"""
    {"t":"Para","c":[{"t":"Code","c":[["",[],[]],"2 + 2"]}]}
    """
  } {
    Para(List(Code(Attr("", Nil, Nil), "2 + 2")))
  }

  "math block" in roundtrip[Block] {
    i"""
    {"t":"Para","c":[{"t":"Math","c":[{"t":"InlineMath","c":[]},"3 + 3"]}]}
    """
  } {
    Para(List(Math(InlineMath, "3 + 3")))
  }

  "inline math" in roundtrip[Block] {
    i"""
    {"t":"Para","c":[{"t":"Math","c":[{"t":"InlineMath","c":[]},"3 + 3"]}]}
    """
  } {
    Para(List(Math(InlineMath, "3 + 3")))
  }

  "image" in roundtrip[Block] {
    i"""
    {"t":"Para","c":[
      {"t":"Image","c":[
        [{"t":"Str","c":"alttext"}],
        ["http://example.com",""]
      ]}
    ]}
    """
  } {
    Para(List(Image(List(Str("alttext")), Target("http://example.com", ""))))
  }

  "link" in roundtrip[Block] {
    i"""
    {"t":"Para","c":[
      {"t":"Link","c":[
        [{"t":"Str","c":"anchor"}],
        ["url",""]
      ]}
    ]}
    """
  } {
    Para(List(Link(List(Str("anchor")), Target("url", ""))))
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

  "div" in roundtrip[Block] {
    i"""
    {"t":"Div","c":[
      ["id",["class1","class2"],[["attr1","value1"],["attr2","value2"]]],
      []
    ]}
    """
  } {
    Div(Attr("id", List("class1", "class2"), List("attr1" -> "value1", "attr2" -> "value2")), Nil)
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

  "definition list" in roundtrip[Block] {
    i"""
    {"t":"DefinitionList","c":[
      [
        [{"t":"Str","c":"term1"}],
        [[{"t":"Plain","c":[{"t":"Str","c":"definition1"}]}]]
      ],
      [
        [{"t":"Str","c":"term2"}],
        [[{"t":"Plain","c":[{"t":"Str","c":"definition2"}]}]]
      ]
    ]}
    """
  } {
    DefinitionList(List(
      DefinitionItem(
        List(Str("term1")),
        List(Definition(List(Plain(List(Str("definition1"))))))
      ),
      DefinitionItem(
        List(Str("term2")),
        List(Definition(List(Plain(List(Str("definition2"))))))
      )
    ))
  }

  "populated table" in roundtrip[Block] {
    i"""
    {"t":"Table","c":[
      [],
      [
        {"t":"AlignDefault","c":[]},
        {"t":"AlignDefault","c":[]},
        {"t":"AlignDefault","c":[]}
      ],
      [0.0,0.0,0.0],
      [
        [{"t":"Plain","c":[{"t":"Str","c":"A"}]}],
        [{"t":"Plain","c":[{"t":"Str","c":"B"}]}],
        [{"t":"Plain","c":[{"t":"Str","c":"C"}]}]
      ],
      [
        [
          [{"t":"Plain","c":[{"t":"Str","c":"1"}]}],
          [{"t":"Plain","c":[{"t":"Str","c":"2"}]}],
          [{"t":"Plain","c":[{"t":"Str","c":"3"}]}]
        ],
        [
          [{"t":"Plain","c":[{"t":"Str","c":"4"}]}],
          [{"t":"Plain","c":[{"t":"Str","c":"5"}]}],
          [{"t":"Plain","c":[{"t":"Str","c":"6"}]}]
        ]
      ]
    ]}
    """
  } {
    Table(
      Nil,
      List(AlignDefault, AlignDefault, AlignDefault),
      List(0.0, 0.0, 0.0),
      List(
        TableCell(List(Plain(List(Str("A"))))),
        TableCell(List(Plain(List(Str("B"))))),
        TableCell(List(Plain(List(Str("C")))))
      ),
      List(
        TableRow(List(
          TableCell(List(Plain(List(Str("1"))))),
          TableCell(List(Plain(List(Str("2"))))),
          TableCell(List(Plain(List(Str("3")))))
        )),
        TableRow(List(
          TableCell(List(Plain(List(Str("4"))))),
          TableCell(List(Plain(List(Str("5"))))),
          TableCell(List(Plain(List(Str("6")))))
        ))
      )
    )
  }
}

trait JsonSpecHelpers {
  self: Matchers =>

  def roundtrip[A: Encoder: Decoder](json: String)(expected: A): Unit = {
    val decoded = unsafeDecode[A](unsafeParse(json))

    // Decode and check for equality:
    decoded should be(expected)

    // Round trip and check for equality again:
    unsafeDecode[A](encode(decoded)) should be(expected)
  }

  private def encode[A: Encoder](value: A): Json =
    Encoder[A].apply(value)

  private def unsafeDecode[A: Decoder](json: Json): A =
    Decoder[A].apply(json.hcursor).toOption.get

  private def unsafeParse(str: String): Json =
    parse(str).toOption.get
}

