ThisBuild / scalaVersion     := "2.13.18"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "pl.belicki"
ThisBuild / organizationName := "belicki"

lazy val root = project
  .in(file("."))
  .settings(
    name := "modbus-tcp-protocol-scala",
    libraryDependencies ++= Nil
  ).aggregate(
    models
  )

lazy val models = project
  .in(file("modules/models"))
  .settings(
    name := "models",
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "3.2.20" % Test
    )
  )
