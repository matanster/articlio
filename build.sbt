import com.typesafe.sbt.SbtStartScript

import spray.revolver.RevolverPlugin._

name := "articlio"

organization  := "com.articlio"

version       := "0.1-SNAPSHOT"

//
// akka & spray
//

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

resolvers += "spray repo" at "http://repo.spray.io"

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %   "spray-can_2.11"     % sprayV,
    "io.spray"            %   "spray-routing_2.11" % sprayV,
    "io.spray"            %   "spray-testkit_2.11" % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"    % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"  % akkaV   % "test",
    "org.specs2"          %%  "specs2-core"   % "2.3.11" % "test"
  )
}

seq(SbtStartScript.startScriptForClassesSettings: _*)

//
// spray revolver, only for development
//

Revolver.settings 

// spray-json
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

libraryDependencies += "org.ahocorasick" % "ahocorasick" % "0.2.3"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.2"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.0.0"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "2.0.2"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "org.slf4j" % "slf4j-nop" % "1.6.4"
)

libraryDependencies += "mysql" % "mysql-connector-java" % "latest.release"

resolvers += "Sonatype releases" at "http://oss.sonatype.org/content/repositories/releases/"

libraryDependencies += "com.github.verbalexpressions" %% "scalaverbalexpression" % "1.0.1"

//
// anorm
//

resolvers += "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/"

libraryDependencies += "com.typesafe.play" %% "anorm" % "2.3.6"

scalacOptions ++= Seq( "-unchecked", "-feature" )

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"

libraryDependencies += "com.adrianhurt" %% "play-bootstrap3" % "0.4-SNAPSHOT"

// lazy val twirl = (project in file(".")).enablePlugins(SbtTwirl)

//
// former reporter repo build.sbt relevant stuff - this has the play framework stuff
//

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  "com.typesafe.slick" %% "slick" % "2.1.0",
  "com.typesafe.play" %% "play-slick" % "0.8.1",
  "mysql" % "mysql-connector-java" % "latest.release"
)     

lazy val play = (project in file(".")).enablePlugins(PlayScala)

//
// Add source folder to play project 
//

unmanagedSourceDirectories in Compile += baseDirectory.value / "src"