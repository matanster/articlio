//resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"
resolvers += "Typesafe repository" at "https://repo.typesafe.com/typesafe/releases/"

//addSbtPlugin("com.typesafe.startscript" % "xsbt-start-script-plugin" % "0.5.1")

addSbtPlugin("com.typesafe.sbt" %% "sbt-start-script" % "0.10.0")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("org.netbeans.nbsbt" % "nbsbt-plugin" % "1.1.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-twirl" % "1.0.4")

// Comment to get more information during initialization
// logLevel := Level.Warn

// The Typesafe repository

// The Play plugin
addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.3.7")

// Play related plugins
addSbtPlugin("com.typesafe.sbt" % "sbt-coffeescript" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-less" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-jshint" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-rjs" % "1.0.1")

addSbtPlugin("com.typesafe.sbt" % "sbt-digest" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-mocha" % "1.0.0")

addSbtPlugin("com.typesafe.sbt" % "sbt-gzip" % "1.0.0")

addSbtPlugin("com.jamesward" % "play-auto-refresh" % "0.0.11")