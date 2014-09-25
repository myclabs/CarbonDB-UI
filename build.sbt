import play.Project._

name := """carbondb-ui"""

version := "1.0"

libraryDependencies ++= Seq(
  cache,
  // WebJars infrastructure
  "org.webjars" % "webjars-locator" % "0.17",
  "org.webjars" %% "webjars-play" % "2.2.1-2",
  // WebJars dependencies
  "org.webjars" % "underscorejs" % "1.6.0-3",
  "org.webjars" % "jquery" % "1.11.1",
  "org.webjars" % "bootstrap" % "3.2.0" exclude("org.webjars", "jquery"),
  "org.webjars" % "angularjs" % "1.2.21" exclude("org.webjars", "jquery"),
  "org.webjars" % "angular-file-upload" % "1.5.0",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "org.webjars" % "angular-tree-control" % "0.2.2",
  "org.glassfish.jersey.core" % "jersey-client" % "2.11" exclude("org.glassfish.hk2", "hk2-utils") exclude("org.glassfish.hk2", "hk2-locator"),
  "org.glassfish.hk2" % "hk2-utils" % "2.2.0-b27",
  "org.glassfish.hk2" % "hk2-locator" % "2.2.0-b27",
  "org.json" % "json" % "20140107",
  "org.mongodb" % "mongo-java-driver" % "2.12.3",
  "org.webjars" % "d3js" % "3.4.11",
  "org.webjars" % "angularjs-nvd3-directives" % "0.0.7-1",
  // required for carbondb reasonner
  "org.apache.jena" % "apache-jena-libs" % "2.10.0",
  "org.la4j" % "la4j" % "0.4.9",
  "com.github.ansell.pellet" % "pellet-core" % "2.3.6-ansell",
  "com.github.ansell.pellet" % "pellet-jena" % "2.3.6-ansell",
  "org.apache.commons" % "commons-lang3" % "3.3.2",
  "org.json" % "json" % "20140107"
)

playScalaSettings

resolvers += "typesafe" at "http://repo.typesafe.com/typesafe/repo"

// This tells Play to optimize this file and its dependencies
requireJs += "main.js"

// The main config file
// See http://requirejs.org/docs/optimization.html#mainConfigFile
requireJsShim := "build.js"

// To completely override the optimization process, use this config option:
//requireNativePath := Some("node r.js -o name=main out=javascript-min/main.min.js")
