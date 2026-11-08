ThisBuild / scalaVersion     := "2.13.18"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "pl.belicki"
ThisBuild / organizationName := "belicki"

val PekkoVersion     = "2.0.0"
val ScalatestVersion = "3.2.20"
val BytesVersion     = "1.6.1"
val Slf4jVersion     = "2.0.20"
val LogbackVersion   = "1.6.6"

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
      "org.scalatest" %% "scalatest" % ScalatestVersion % Test
    )
  )

lazy val communication = project
  .in(file("modules/modbus-tcp-protocol-communication"))
  .dependsOn(models % "test->test;compile->compile")
  .settings(
    name := "modbus-tcp-protocol-communication",
    libraryDependencies ++= List(
      "org.apache.pekko" %% "pekko-actor"     % PekkoVersion,
      "org.apache.pekko" %% "pekko-testkit"   % PekkoVersion % Test,
      "at.favre.lib"      % "bytes"           % BytesVersion,
      "org.apache.pekko" %% "pekko-slf4j"     % PekkoVersion,
      "ch.qos.logback"    % "logback-classic" % LogbackVersion,
      "org.slf4j"         % "slf4j-api"       % Slf4jVersion
    )
  )
