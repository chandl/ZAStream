name := """ZAStream"""

version := "1.0"

lazy val root = (project in file(".")).enablePlugins(PlayJava, PlayEbean)

scalaVersion := "2.11.7"

libraryDependencies += javaJdbc

libraryDependencies ++= Seq(
  javaJdbc,
  cache,
  javaWs,
  "mysql" % "mysql-connector-java" % "5.1.40",
  "org.mindrot" % "jbcrypt" % "0.4",
  "com.typesafe.play" %% "play-mailer" % "5.0.0"
)
