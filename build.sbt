lazy val baseSettings = Seq(
  organization := "io.rbricks",
  scalaVersion := "2.12.0",
  crossScalaVersions := Seq("2.11.8", "2.12.0"),
  version := "0.3-SNAPSHOT",
  licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
)

lazy val commonSettings = baseSettings ++ Seq(
  libraryDependencies ++= Seq(
    "org.scalatest"  %% "scalatest"              % "3.0.0"       % "test",
    "com.lihaoyi"    %% "utest"                  % "0.4.4"       % "test",
    "com.lihaoyi"    %% "pprint"                 % "0.4.4"       % "test"
  )
)

lazy val publishSettings = Seq(
  publishMavenStyle := true,
  pomExtra in Global := {
    <url>http://github.com/rbricks/itemized</url>
    <scm>
      <connection>scm:git:github.com/rbricks/itemized.git</connection>
      <developerConnection>scm:git:git@github.com:rbricks/itemized.git</developerConnection>
      <url>github.com/rbricks/itemized</url>
    </scm>
    <developers>
      <developer>
        <id>utaal</id>
        <name>Andrea Lattuada</name>
        <url>http://github.com/utaal</url>
      </developer>
    </developers>
  }
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

val core = (project in file("core"))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name := "itemized",
    libraryDependencies += "org.scala-lang" % "scala-reflect" % scalaVersion.value,
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
  )

val circe = (project in file("circe"))
  .settings(commonSettings)
  .settings(publishSettings)
  .settings(
    name := "itemized-circe",
    libraryDependencies += "io.circe" %% "circe-core" % "0.6.1",
    addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
    testFrameworks += new TestFramework("utest.runner.Framework")
  )
  .dependsOn(core)

val root = (project in file("."))
  .settings(commonSettings)
  .settings(noPublishSettings)
  .aggregate(core, circe)
