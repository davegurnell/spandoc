organization := "com.davegurnell"
name         := "spandoc"
version      := "0.2.0-SNAPSHOT"
scalaVersion := "2.11.8"

licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation"
)

libraryDependencies ++= Seq(
  "org.typelevel"   %% "cats"          % "0.4.1",
  "io.circe"        %% "circe-core"    % "0.4.0",
  "io.circe"        %% "circe-generic" % "0.4.0",
  "io.circe"        %% "circe-parser"  % "0.4.0",
  "com.davegurnell" %% "unindent"      % "1.0.0" % "test",
  "org.scalatest"   %% "scalatest"     % "2.2.4" % "test"
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
