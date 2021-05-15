enablePlugins(GitVersioning)
enablePlugins(GitBranchPrompt)

// Basic settings -------------------------------

organization := "com.davegurnell"
name := "spandoc"

ThisBuild / scalaVersion := "2.13.5"

ThisBuild / crossScalaVersions := Seq("2.13.5", "2.12.13")

ThisBuild / scalacOptions ++= Seq(
  "-feature",
  "-unchecked",
  "-deprecation"
)

ThisBuild / libraryDependencies ++= Seq(
  "org.typelevel"   %% "cats-core"     % "2.6.1",
  "io.circe"        %% "circe-core"    % "0.13.0",
  "io.circe"        %% "circe-generic" % "0.13.0",
  "io.circe"        %% "circe-parser"  % "0.13.0",
  "com.davegurnell" %% "unindent"      % "1.6.0"  % Test,
  "org.scalameta"   %% "munit"         % "0.7.26" % Test
)

// Versioning -----------------------------------

ThisBuild / versionScheme := Some("early-semver")

git.gitUncommittedChanges := git.gitCurrentTags.value.isEmpty // Put "-SNAPSHOT" on a commit if it's not a tag

// Github Actions -------------------------------

ThisBuild / githubWorkflowJavaVersions := Seq("adopt@1.11")

// Publishing -----------------------------------

usePgpKeyHex("932DAC1231EE4ACF")

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")

ThisBuild / githubWorkflowPublishTargetBranches := Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(
    List("ci-release"),
    env = Map(
      "PGP_PASSPHRASE"    -> "${{ secrets.PGP_PASSPHRASE }}",
      "PGP_SECRET"        -> "${{ secrets.PGP_SECRET }}",
      "SONATYPE_PASSWORD" -> "${{ secrets.SONATYPE_PASSWORD }}",
      "SONATYPE_USERNAME" -> "${{ secrets.SONATYPE_USERNAME }}"
    )
  )
)

ThisBuild / licenses += ("Apache-2.0", url("http://apache.org/licenses/LICENSE-2.0"))

ThisBuild / homepage := Some(url("https://github.com/davegurnell/spandoc"))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("https://github.com/davegurnell/spandoc.git"),
    "scm:git@github.com:davegurnell/spandoc.git"
  )
)

ThisBuild / developers := List(
  Developer(
    id = "davegurnell",
    name = "Dave Gurnell",
    email = "dave@underscore.io",
    url = url("https://twitter.com/davegurnell")
  )
)
