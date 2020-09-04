organization       := "com.davegurnell"
name               := "spandoc"
scalaVersion       := "2.13.3"
crossScalaVersions := Seq("2.12.12", "2.13.3")

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation"
)

libraryDependencies ++= Seq(
  "org.typelevel"   %% "cats-core"     % "2.2.0",
  "io.circe"        %% "circe-core"    % "0.13.0",
  "io.circe"        %% "circe-generic" % "0.13.0",
  "io.circe"        %% "circe-parser"  % "0.13.0",
  "com.davegurnell" %% "unindent"      % "1.1.1" % Test,
  "org.scalatest"   %% "scalatest"     % "3.0.8" % Test
)

homepage := Some(url("https://github.com/davegurnell/spandoc"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/davegurnell/spandoc.git"),
    "scm:git@github.com:davegurnell/spandoc.git"
  )
)

developers := List(
  Developer(
    id = "davegurnell",
    name = "Dave Gurnell",
    email = "dave@underscore.io",
    url = url("https://twitter.com/davegurnell")
  )
)

pgpPublicRing := file("./travis/local.pubring.asc")
pgpSecretRing := file("./travis/local.secring.asc")
releaseEarlyWith := SonatypePublisher

// Command Aliases

addCommandAlias("ci", ";clean ;coverage ;compile ;+test ;coverageReport")

addCommandAlias("release", ";releaseEarly")
