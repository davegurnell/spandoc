# Spandoc

Write Pandoc filters in Scala. Very early release. Still in development.

Copyright 2016 Dave Gurnell. Licensed [Apache 2][license].

[![Build Status](https://travis-ci.org/davegurnell/spandoc.svg?branch=develop)](https://travis-ci.org/davegurnell/spandoc)
[![Coverage status](https://img.shields.io/codecov/c/github/davegurnell/spandoc/develop.svg)](https://codecov.io/github/davegurnell/spandoc)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.davegurnell/spandoc_2.13/badge.svg)](https://maven-badges.herokuapp.com/maven-central/com.davegurnell/spandoc_2.13)

## Getting Started

Grab this library in an Ammonite script and use it with Pandoc's `--filter` parameter.
Here's an example script:

~~~ scala
// Filename: filter.scala
#!/usr/bin/env amm

import ammonite.repl._

interp.load.ivy("com.davegurnell" %% "spandoc" % "<<VERSION>>")

@

import spandoc._

// Define a transform. This one only transforms "inline" elements:
val uppercase = transofrm.TopDown.inline {
  case Str(str) =>
    Str(str.toUpperCase)
}

// Run the transform on stdin, printing the result to stdout:
transformStdin(uppercase)
~~~

Then run Pandoc using the `--filter` option to point to `filter.scala`:

~~~ bash
bash$ echo 'Lorem ipsum' | pandoc --to=html --filter=./filter.scala
<p>LOREM IPSUM</p>
~~~

[license]: http://www.apache.org/licenses/LICENSE-2.0
