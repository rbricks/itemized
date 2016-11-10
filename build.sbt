name := "itemized"
organization := "io.rbricks"

scalaVersion := "2.11.8"

version      := "0.0.1"

libraryDependencies += { "org.scala-lang" % "scala-reflect" % scalaVersion.value }

libraryDependencies ++= Seq(
  "org.scalatest"  %% "scalatest"     % "2.2.0" % "test",
  "org.mockito"    %  "mockito-all"   % "1.9.5" % "test"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

licenses += ("MIT", url("http://opensource.org/licenses/MIT"))

publishMavenStyle := true

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
