import sbt.Keys.cancelable
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy

name := "memento"
organization := "com.github.reugn"
scalaVersion := "2.12.11"
crossScalaVersions := Seq(scalaVersion.value, "2.13.2")

val kafkaVersion = "2.5.0"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.0",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "javax.inject" % "javax.inject" % "1",
  "com.google.inject" % "guice" % "4.2.3",
  "com.typesafe.akka" %% "akka-actor" % "2.6.5",
  "com.typesafe.akka" %% "akka-stream" % "2.6.5",
  "com.typesafe.akka" %% "akka-http" % "10.1.12",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.1.12",
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % "3.5",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.scalatest" %% "scalatest" % "3.1.2" % Test
)

excludeDependencies ++= Seq(
  "org.slf4j" % "slf4j-log4j12",
  "log4j" % "log4j"
)

scalacOptions := Seq(
  "-target:jvm-1.8",
  "-unchecked",
  "-deprecation",
  "-feature",
  "-encoding", "utf8",
  "-Xlint:-missing-interpolator"
)

mainClass in(Compile, run) := Some("com.github.reugn.memento.MementoApp")

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

assemblyOutputPath in assembly := baseDirectory.value /
  "assembly" / (name.value + "-" + version.value + ".jar")

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs@_*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

unmanagedResourceDirectories in Compile += baseDirectory.value / "conf"
cancelable in Global := true
