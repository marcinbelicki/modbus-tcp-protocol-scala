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
    models,
    communication
  )

lazy val models = project
  .in(file("modules/modbus-tcp-protocol-models"))
  .settings(
    name := "modbus-tcp-protocol-models",
    libraryDependencies ++= List(
      "org.scalatest" %% "scalatest" % "3.2.20" % Test
    )
  )

lazy val communication = project
  .in(file("modules/modbus-tcp-protocol-communication"))
  .dependsOn(models % "test->test;compile->compile")
  .settings(
    name := "modbus-tcp-protocol-communication"
  )
