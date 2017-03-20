organization      := "com.davegurnell"
name              := "spandoc"
version           := "0.2.1"

scalaOrganization := "org.typelevel"
scalaVersion      := "2.12.1"

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation"
)

libraryDependencies ++= Seq(
  "org.typelevel"   %% "cats-core"     % "0.9.0",
  "io.circe"        %% "circe-core"    % "0.7.0",
  "io.circe"        %% "circe-generic" % "0.7.0",
  "io.circe"        %% "circe-parser"  % "0.7.0",
  "com.davegurnell" %% "unindent"      % "1.1.0" % Test,
  "org.scalatest"   %% "scalatest"     % "3.0.1" % Test
)

pomExtra in Global := {
  <url>https://github.com/davegurnell/spandoc</url>
  <scm>
    <connection>scm:git:github.com/davegurnell/spandoc</connection>
    <developerConnection>scm:git:git@github.com:davegurnell/spandoc</developerConnection>
    <url>github.com/davegurnell/spandoc</url>
  </scm>
  <developers>
    <developer>
      <id>davegurnell</id>
      <name>Dave Gurnell</name>
      <url>http://davegurnell.com</url>
      <organization>Underscore</organization>
      <organizationUrl>http://underscore.io</organizationUrl>
    </developer>
  </developers>
}
