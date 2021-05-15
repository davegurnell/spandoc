package spandoc

import io.circe._
import io.circe.jawn._
import io.circe.syntax._
import unindent._
import spandoc.ast._

class JsonSpec extends munit.FunSuite with Encoders with Decoders {

  test("pandoc") {
    assertRoundtrip[Pandoc](
      i"""
      {
        "blocks":[],
        "meta":{},
        "pandoc-api-version":[1,20]
      }
      """,
      Pandoc(Vector.empty)
    )
  }

  test("meta") {
    assertRoundtrip[Meta](
      i"""
      {
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
      }
      """,
      Meta(
        Map(
          "a" -> MetaString("1"),
          "b" -> MetaInlines(Vector(Str("foo"))),
          "c" -> MetaList(
            Vector(
              MetaString("1"),
              MetaString("2"),
              MetaString("3")
            )
          ),
          "d" -> MetaList(
            Vector(
              MetaInlines(Vector(Str("e"))),
              MetaInlines(Vector(Str("f")))
            )
          ),
          "g" -> MetaBool(true),
          "h" -> MetaBlocks(
            Vector(
              Para(Vector(Str("lorem"), Space, Str("ipsum")))
            )
          )
        )
      )
    )
  }

  test("paragraph") {
    assertRoundtrip[Block](
      i"""
      {"t":"Para", "c":[
        {"t":"Str","c":"lorem"},
        {"t":"Space","c":[]},
        {"t":"Str","c":"ipsum"}
      ]}
      """,
      Para(Vector(Str("lorem"), Space, Str("ipsum")))
    )
  }

  test("code block") {
    assertRoundtrip[Block](
      i"""
      {"t":"CodeBlock","c":[["",["scala"],[]],"1 + 1"]}
      """,
      CodeBlock(Attr("", List("scala")), "1 + 1")
    )
  }

  test("raw block") {
    assertRoundtrip[Block](
      i"""
      {"t":"RawBlock","c":["latex", "foo"]}
      """,
      RawBlock("latex", "foo")
    )
  }

  test("inline code") {
    assertRoundtrip[Block](
      i"""
      {"t":"Para","c":[{"t":"Code","c":[["",[],[]],"2 + 2"]}]}
      """,
      Para(Vector(Code(Attr.empty, "2 + 2")))
    )
  }

  test("math block") {
    assertRoundtrip[Block](
      i"""
      {"t":"Para","c":[{"t":"Math","c":[{"t":"InlineMath","c":[]},"3 + 3"]}]}
      """,
      Para(Vector(Math(InlineMath, "3 + 3")))
    )
  }

  test("inline math") {
    assertRoundtrip[Block](
      i"""
      {"t":"Para","c":[{"t":"Math","c":[{"t":"InlineMath","c":[]},"3 + 3"]}]}
      """,
      Para(Vector(Math(InlineMath, "3 + 3")))
    )
  }

  test("image") {
    assertRoundtrip[Block](
      i"""
      {"t":"Para","c":[
        {"t":"Image","c":[
          ["", [], []],
          [{"t":"Str","c":"alttext"}],
          ["http://example.com",""]
        ]}
      ]}
      """,
      Para(Vector(Image(Attr.empty, Vector(Str("alttext")), Target("http://example.com", ""))))
    )
  }

  test("link") {
    assertRoundtrip[Block](
      i"""
      {"t":"Para","c":[
        {"t":"Link","c":[
          ["", [], []],
          [{"t":"Str","c":"anchor"}],
          ["url",""]
        ]}
      ]}
      """,
      Para(Vector(Link(Attr.empty, Vector(Str("anchor")), Target("url", ""))))
    )
  }

  test("heading") {
    assertRoundtrip[Block](
      i"""
      {"t": "Header", "c": [
        1,
        ["heading",[],[]],
        [{"t": "Str", "c": "heading 1"}]
      ]}
      """,
      Header(1, Attr("heading"), Vector(Str("heading 1")))
    )
  }

  test("div") {
    assertRoundtrip[Block](
      i"""
      {"t":"Div","c":[
        ["id",["class1","class2"],[["attr1","value1"],["attr2","value2"]]],
        []
      ]}
      """,
      Div(Attr("id", List("class1", "class2"), List("attr1" -> "value1", "attr2" -> "value2")), Vector.empty)
    )
  }

  test("bullet list") {
    assertRoundtrip[Block](
      i"""
      {"t": "BulletList", "c": [
        [{"t": "Plain", "c": [{"t": "Str", "c": "item1"}]}],
        [{"t": "Plain", "c": [{"t": "Str", "c": "item2"}]}],
        [{"t": "Plain", "c": [{"t": "Str", "c": "item3"}]}]
      ]}
      """,
      BulletList(
        Vector(
          ListItem(Vector(Plain(Vector(Str("item1"))))),
          ListItem(Vector(Plain(Vector(Str("item2"))))),
          ListItem(Vector(Plain(Vector(Str("item3")))))
        )
      )
    )
  }

  test("nested bullet lists") {
    assertRoundtrip[Block](
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
      """,
      BulletList(
        Vector(
          ListItem(
            Vector(
              Plain(Vector(Str("item1"))),
              BulletList(
                Vector(
                  ListItem(Vector(Plain(Vector(Str("item1a"))))),
                  ListItem(Vector(Plain(Vector(Str("item1b")))))
                )
              )
            )
          ),
          ListItem(
            Vector(
              Plain(Vector(Str("item2"))),
              BulletList(
                Vector(
                  ListItem(Vector(Plain(Vector(Str("item2a"))))),
                  ListItem(Vector(Plain(Vector(Str("item2b")))))
                )
              )
            )
          )
        )
      )
    )
  }

  test("ordered list") {
    assertRoundtrip[Block](
      i"""
      {"t":"OrderedList", "c":[
        [1,{"t":"Decimal","c":[]},{"t":"Period","c":[]}],
        [
          [{"t":"Plain","c":[{"t":"Str","c":"item1"}]}],
          [{"t":"Plain","c":[{"t":"Str","c":"item2"}]}],
          [{"t":"Plain","c":[{"t":"Str","c":"item3"}]}]
        ]
      ]}
      """,
      OrderedList(
        ListAttributes(1, Decimal, Period),
        Vector(
          ListItem(Vector(Plain(Vector(Str("item1"))))),
          ListItem(Vector(Plain(Vector(Str("item2"))))),
          ListItem(Vector(Plain(Vector(Str("item3")))))
        )
      )
    )
  }

  test("nested ordered lists") {
    assertRoundtrip[Block](
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
      """,
      OrderedList(
        ListAttributes(1, Decimal, Period),
        Vector(
          ListItem(
            Vector(
              Plain(Vector(Str("item1"))),
              OrderedList(
                ListAttributes(1, LowerAlpha, Period),
                Vector(
                  ListItem(Vector(Plain(Vector(Str("item1a"))))),
                  ListItem(Vector(Plain(Vector(Str("item1b")))))
                )
              )
            )
          ),
          ListItem(
            Vector(
              Plain(Vector(Str("item2"))),
              OrderedList(
                ListAttributes(1, LowerAlpha, Period),
                Vector(
                  ListItem(Vector(Plain(Vector(Str("item2a"))))),
                  ListItem(Vector(Plain(Vector(Str("item2b")))))
                )
              )
            )
          )
        )
      )
    )
  }

  test("definition list") {
    assertRoundtrip[Block](
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
      """,
      DefinitionList(
        Vector(
          DefinitionItem(
            Vector(Str("term1")),
            Vector(Definition(Vector(Plain(Vector(Str("definition1"))))))
          ),
          DefinitionItem(
            Vector(Str("term2")),
            Vector(Definition(Vector(Plain(Vector(Str("definition2"))))))
          )
        )
      )
    )
  }

  test("populated table") {
    assertRoundtrip[Block](
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
      """,
      Table(
        Vector.empty,
        Vector(AlignDefault, AlignDefault, AlignDefault),
        Vector(0.0, 0.0, 0.0),
        Vector(
          TableCell(Vector(Plain(Vector(Str("A"))))),
          TableCell(Vector(Plain(Vector(Str("B"))))),
          TableCell(Vector(Plain(Vector(Str("C")))))
        ),
        Vector(
          TableRow(
            Vector(
              TableCell(Vector(Plain(Vector(Str("1"))))),
              TableCell(Vector(Plain(Vector(Str("2"))))),
              TableCell(Vector(Plain(Vector(Str("3")))))
            )
          ),
          TableRow(
            Vector(
              TableCell(Vector(Plain(Vector(Str("4"))))),
              TableCell(Vector(Plain(Vector(Str("5"))))),
              TableCell(Vector(Plain(Vector(Str("6")))))
            )
          )
        )
      )
    )
  }

  def assertRoundtrip[A: Encoder: Decoder](json: String, expected: A): Unit = {
    val decoded = unsafeDecode[A](unsafeParse(json))

    // Decode and check for equality:
    assert(clue(decoded) == clue(expected))

    // Round trip and check for equality again:
    assert(unsafeDecode[A](encode(decoded)) == expected)
  }

  private def encode[A: Encoder](value: A): Json =
    Encoder[A].apply(value)

  private def unsafeDecode[A: Decoder](json: Json): A =
    Decoder[A].apply(json.hcursor).toOption.get

  private def unsafeParse(str: String): Json =
    parse(str).toOption.get
}
