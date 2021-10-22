import sbt.Keys.cancelable
import sbtassembly.AssemblyPlugin.autoImport.assemblyMergeStrategy

name := "memento"
organization := "com.github.reugn"
scalaVersion := "2.12.15"
crossScalaVersions := Seq(scalaVersion.value, "2.13.6")

val kafkaVersion = "2.8.1"
val akkaVersion = "2.6.15"
val akkaHttpVersion = "10.2.6"

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.4.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "javax.inject" % "javax.inject" % "1",
  "com.google.inject" % "guice" % "5.0.1",
  "com.typesafe.akka" %% "akka-actor" % akkaVersion,
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "org.apache.kafka" % "kafka-clients" % kafkaVersion,
  "org.apache.kafka" %% "kafka" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams" % kafkaVersion,
  "org.apache.kafka" % "kafka-streams-test-utils" % kafkaVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % "6.6",
  "ch.qos.logback" % "logback-classic" % "1.2.6",
  "org.scalatest" %% "scalatest" % "3.2.10" % Test
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

Compile / run / mainClass := Some("com.github.reugn.memento.MementoApp")

licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0.html"))

assembly / assemblyOutputPath := baseDirectory.value /
  "assembly" / (name.value + "-" + version.value + ".jar")

assembly / assemblyMergeStrategy := {
  case PathList("META-INF", _*) => MergeStrategy.discard
  case PathList("reference.conf") => MergeStrategy.concat
  case _ => MergeStrategy.first
}

Compile / unmanagedResourceDirectories += baseDirectory.value / "conf"
Global / cancelable := true
