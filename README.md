# Spandoc

Write Pandoc filters in Scala. Very early release. Still in development.

Copyright 2016 Dave Gurnell. Licensed [Apache 2][license].

[![Scala 2.12](https://img.shields.io/maven-central/v/com.davegurnell/spandoc_2.12?label=Scala%202.12)](https://search.maven.org/artifact/com.davegurnell/spandoc_2.12)
[![Scala 2.13](https://img.shields.io/maven-central/v/com.davegurnell/spandoc_2.13?label=Scala%202.13)](https://search.maven.org/artifact/com.davegurnell/spandoc_2.13)

## Requirements

Spandoc requires:

- [Pandoc][pandoc] (tested with Pandoc 2.9.1.1);
- [Ammonite][ammonite] (to create Spandoc shell scripts).

## Getting Started

Use Spandoc in an Ammonite and use it with Pandoc's `--filter` parameter:

```bash
echo 'Lorem ipsum' | pandoc --to=html --filter=my-filter.sc
# <p>LOREM IPSUM</p>
```

Here's an example script:

```scala
// Filename: my-filter.sc
#!/usr/bin/env amm

interp.load.ivy("com.davegurnell" %% "spandoc" % "<<VERSION>>")

@
import spandoc._, ast._, transform._

// An AST transform that uppercases inline text:
val uppercase = TopDown.inline {
  case Str(str) => Str(str.toUpperCase)
}

// Run the transform on stdin, printing the result to stdout:
transformStdin(uppercase)
```

## How to Create Transforms

A transform is simply a function of the following type:

```scala
import spandoc.ast.Pandoc

type TransformFunc = Pandoc => Pandoc
```

Spandoc provides some helper classes
to assist in the creation of transforms:

- `spandoc.transform.Transform` is a class that breaks the transform
  down into smaller components: `Block` nodes, `Inline` nodes, and so on;

- `spandoc.transform.BottomUp` implements `Block` and `Inline` transforms
  in a bottom-up traversal order that processes every node in the AST;

- `spandoc.transform.TopDown` implements `Block` and `Inline` transforms
  in a top-down traversal order that allows you to shortcut transformation of subtrees.

You can create simple transforms using the methods on the companion objects of
`BottomUp` and `TopDown` (as in the example above),
or you can extend either type to create a more complex transform:

```scala
object transform extends BottomUp[cats.Id] {
  override def blockTransform = {
    // Change all ordered lists to bulleted lists:
    case OrderedList(_, items) => BulletList(items)
  }

  override def inlineTransform - {
    // Uppercase all body text:
    case Str(str) => Str(str.toUpperCase)
  }
}
```

Transforms can be monadic, which is typically useful
to thread a `State` monad through the AST.
See the `examples` directory for use cases like this.

[license]: http://www.apache.org/licenses/LICENSE-2.0
[pandoc]: https://pandoc.org/
[ammonite]: https://ammonite.io/
