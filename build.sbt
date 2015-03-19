import com.typesafe.sbt.SbtStartScript

import spray.revolver.RevolverPlugin._

name := "articlio"

organization  := "com.articlio"

version       := "0.1-SNAPSHOT"

//
// akka & spray
//

scalaVersion  := "2.11.5"

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
// spray revolver, only for development, not really relevant with play framework
//

Revolver.settings 

// spray-json
libraryDependencies += "io.spray" %%  "spray-json" % "1.3.1"

libraryDependencies += "org.ahocorasick" % "ahocorasick" % "0.2.3"

libraryDependencies += "org.sorm-framework" % "sorm" % "0.3.16"

libraryDependencies += "org.scala-lang" % "scala-compiler" % "2.11.2"

libraryDependencies += "com.github.tototoshi" %% "scala-csv" % "1.0.0"

libraryDependencies += "com.google.code.findbugs" % "jsr305" % "2.0.2"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % "3.0.0-RC1",
  "com.zaxxer" % "HikariCP-java6" % "2.0.1",
  "org.slf4j" % "slf4j-nop" % "1.6.4")

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

//libraryDependencies += "com.adrianhurt" %% "play-bootstrap3" % "0.4-SNAPSHOT"

// lazy val twirl = (project in file(".")).enablePlugins(SbtTwirl)

//
// former reporter repo build.sbt relevant stuff - this has the play framework stuff
//

libraryDependencies ++= Seq(
  jdbc,
  anorm,
  cache,
  ws,
  //"com.typesafe.play" %% "play-slick" % "0.8.1",
  "mysql" % "mysql-connector-java" % "latest.release"
)     

lazy val play = (project in file(".")).enablePlugins(PlayScala)

//
// Add source folder to play project 
//

unmanagedSourceDirectories in Compile += baseDirectory.value / "src"

//
// enable multiple routes files - this wasn't enough though, attempt abandoned
//

scalacOptions ++= Seq(
  "-feature", // Shows warnings in detail in the stdout
  "-language:reflectiveCalls" 
)

//
// SistaNLP
//

libraryDependencies ++= {
  //val version = "4.0-SNAPSHOT"
  val version = "3.3"
  Seq("edu.arizona.sista" % "processors" % version,
      "edu.arizona.sista" % "processors" % version classifier "models")
}

//
// enable play http client calls
//

libraryDependencies ++= Seq(
  ws
)

//
// avoid the play auto refresh plugin opening a browser window on startup 
//

com.jamesward.play.BrowserNotifierKeys.shouldOpenBrowser := false

//
// Riak
//
libraryDependencies += "com.basho.riak" % "riak-client" % "2.0.0"


//
// dummy task that does nothing
//
lazy val dummytask = taskKey[Unit]("dummy task")

dummytask := {
  println("Running task that does nothing...")
  println("...done.")
}

//
// sbt task that auto-generates Slick classes for a given existing database. Usage: sbt slickGenerate
// This doesn't work yet - see http://stackoverflow.com/questions/28511698/adding-task-to-sbt-13-x-build-sbt.
// so the script slickGenerate.sh takes care of this instead (relying on a sibling repo)
//

//libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "2.1.0"
libraryDependencies += "com.typesafe.slick" %% "slick-codegen" % "3.0.0-RC1"

lazy val slickGenerate = TaskKey[Seq[File]]("slick code generation")

slickGenerate <<= slickGenerateTask 

lazy val slickGenerateTask = {
    (sourceManaged in Compile, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
      val dbName = "articlio"
      val userName = "articlio"
      val password = "" // no password for this user
      val url = s"jdbc:mysql://localhost:3306/$dbName" 
      val jdbcDriver = "com.mysql.jdbc.Driver"
      val slickDriver = "scala.slick.driver.MySQLDriver"
      val targetPackageName = "models"
      val outputDir = (dir / dbName).getPath // place generated files in sbt's managed sources folder
      val fname = outputDir + s"/$targetPackageName/Tables.scala"
      println(s"\nauto-generating slick source for database schema at $url...")
      println(s"output source file file: file://$fname\n")
      r.run("scala.slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, targetPackageName, userName, password), s.log)
      Seq(file(fname))
    }
}

//
// workaround / fix for http://stackoverflow.com/questions/28104968/scala-ide-4-0-0-thinks-theres-errors-in-an-out-of-the-box-play-framework-2-3-7/28550840#28550840 (tentatively related: https://github.com/typesafehub/sbteclipse/pull/242)
//
//EclipseKeys.createSrc := EclipseCreateSrc.All

//
// rackspace api
//

libraryDependencies ++= Seq("org.apache.jclouds.driver" % "jclouds-slf4j" % "1.8.1",
"org.apache.jclouds.driver" % "jclouds-sshj" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-cloudservers-us" % "1.8.1",
"org.apache.jclouds.labs" % "rackspace-cloudfiles-us" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-cloudblockstorage-us" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-clouddatabases-us" % "1.8.1",
"org.apache.jclouds.labs" % "rackspace-cloudqueues-us" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-cloudloadbalancers-us" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-clouddns-us" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-cloudservers-uk" % "1.8.1",
"org.apache.jclouds.labs" % "rackspace-cloudfiles-uk" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-cloudblockstorage-uk" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-clouddatabases-uk" % "1.8.1",
"org.apache.jclouds.labs" % "rackspace-cloudqueues-uk" % "1.8.1",
"org.apache.jclouds.labs" % "rackspace-autoscale-us" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-cloudloadbalancers-uk" % "1.8.1",
"org.apache.jclouds.provider" % "rackspace-clouddns-uk" % "1.8.1"
)

//
// testing 
//
libraryDependencies += "com.h2database" % "h2" % "1.4.186"

