name := "ingredients-caseenum"

version       := "0.1-SNAPSHOT"

resolvers += "Pellucid Bintray" at "http://dl.bintray.com/pellucid/maven"

libraryDependencies <+= (scalaVersion) { sv =>
  "org.scala-lang" %  "scala-reflect" % sv
}

libraryDependencies ++= Seq(
  "com.pellucid"   %% "sealerate"     % "0.0.3",
  "org.scalatest"  %% "scalatest"     % "2.2.0" % "test",
  "org.mockito"    %  "mockito-all"   % "1.9.5" % "test"
)

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0-M5" cross CrossVersion.full)
