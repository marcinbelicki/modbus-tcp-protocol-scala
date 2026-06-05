ThisBuild / scalaVersion     := "2.13.18"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "pl.belicki"
ThisBuild / organizationName := "belicki"

lazy val root = (project in file("."))
  .settings(
    name := "modbus-tcp-models",
    libraryDependencies ++= Nil

  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
