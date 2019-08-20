
name := "helloshiftleft-scala"
organization := "io.shiftleft"

version := "0.0.1-SNAPSHOT"

val playVersion = "2.6.7"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

crossPaths := false

scalaVersion := "2.12.4"

libraryDependencies ++= Seq(
  guice,
  javaJpa,
  evolutions,
  jdbc,
  ws,
  "org.hibernate" % "hibernate-entitymanager" % "5.2.12.Final",
  "org.projectlombok" % "lombok" % "1.16.18",
  "org.apache.commons" % "commons-collections4" % "4.1",
  "org.apache.commons" % "commons-io" % "1.3.2",
  "mysql" % "mysql-connector-java" % "8.0.11",
  "commons-collections" % "commons-collections" % "3.1",
  "org.apache.wicket" % "wicket-util" % "6.23.0",
  "aopalliance" % "aopalliance" % "1.0",
  "org.javassist" % "javassist" % "3.22.0-GA",
  "com.mchange" % "mchange-commons-java" % "0.2.11",
  "org.clojure" % "clojure" % "1.8.0",
  "com.typesafe" % "config" % "1.3.2",
  "org.springframework" % "spring-expression" % "4.1.4.RELEASE",
)

// enforce slightly older jackson-databind, before the CVE-2017-7525 fix
dependencyOverrides ++= Seq(
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.8.5",
  "commons-beanutils" % "commons-beanutils" % "1.9.2",
  "commons-logging" % "commons-logging" % "1.2",
  "org.javassist" % "javassist" % "3.22.0-GA",
  "com.mchange" % "mchange-commons-java" % "0.2.11",
  "org.clojure" % "clojure" % "1.8.0",
  "org.codehaus.groovy" % "groovy" % "2.3.9",
  "org.springframework" % "spring-expression" % "4.1.4.RELEASE",
  "org.springframework" % "spring-beans" % "4.1.4.RELEASE",
  "org.springframework" % "spring-context" % "4.1.4.RELEASE",
  "org.springframework" % "spring-core" % "4.1.4.RELEASE",
  "commons-collections" % "commons-collections" % "3.1",
  "org.apache.wicket" % "wicket-util" % "6.23.0",
  "aopalliance" % "aopalliance" % "1.0",
  "com.typesafe.play" % "play-jdbc_2.12" % playVersion,
  "com.typesafe.play" % "play-jdbc-api_2.12" % playVersion,
  "com.typesafe.play" % "play-jdbc-evolutions_2.12" % playVersion,
  "com.typesafe.play" % "play-java-jdbc_2.12" % playVersion
)
PlayKeys.devSettings := Seq("play.server.http.address" -> "127.0.0.1",
                            "play.server.http.port" -> "8082")

// Big standalone jar configuration (not officially supported by Play)

assembly/mainClass := Some("play.core.server.ProdServerStart")
assembly/fullClasspath += Attributed.blank(PlayKeys.playPackageAssets.value)

assembly/assemblyMergeStrategy := {
  case PathList("javax", "persistence", xs @ _*)     => MergeStrategy.last
  case PathList("javax", "transaction", xs @ _*)     => MergeStrategy.last
  case PathList("org", "apache", "commons", "logging", xs @ _*)     => MergeStrategy.last
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".sql"  => MergeStrategy.first
  case "application.conf"                            => MergeStrategy.concat
  case "unwanted.txt"                                => MergeStrategy.discard
  case "play/reference-overrides.conf"               => MergeStrategy.first
  case x =>
    val oldStrategy = (assembly/assemblyMergeStrategy).value
    oldStrategy(x)
}

artifact in (Compile, assembly) := {
  val art = (artifact in (Compile, assembly)).value
  art.withClassifier(Some("assembly"))
}

addArtifact(artifact in (Compile, assembly), assembly)

