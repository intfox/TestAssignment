name := "TestAssignment"

version := "0.1"

scalaVersion := "2.13.1"

val Http4sVersion = "0.21.1"
val circeVersion = "0.12.3"

libraryDependencies ++= Seq(
  "org.http4s"            %% "http4s-blaze-server" % Http4sVersion,
  "org.http4s"            %% "http4s-blaze-client" % Http4sVersion,
  "org.http4s"            %% "http4s-dsl"          % Http4sVersion,
  "org.http4s"            %% "http4s-circe"        % Http4sVersion,
  "io.circe"              %% "circe-core"          % circeVersion,
  "io.circe"              %% "circe-generic"       % circeVersion,
  "io.circe"              %% "circe-parser"        % circeVersion,
  "ch.qos.logback"        % "logback-classic"      % "1.2.3",
  "org.scalatest"         %% "scalatest"           % "3.1.1" % "test",
  "com.github.pureconfig" %% "pureconfig"          % "0.12.3"
)

scalacOptions ++= Seq("-language:higherKinds", "-Ymacro-annotations")

assemblyJarName in assembly := "TestAssignment.jar"
mainClass in assembly := Some("Server")
