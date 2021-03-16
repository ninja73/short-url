name := "url-short-service"

version := "0.1"

scalaVersion := "2.13.5"

scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature", "-Xfatal-warnings")

enablePlugins(DockerPlugin)

val ScalaLoggingVersion = "3.9.2"
val AkkaVersion = "2.6.8"
val AkkaHttpVersion = "10.2.4"
val AkkaHttpCirceVersion = "1.35.3"
val CirceVersion = "0.12.3"
val HashidsScalaVersion = "1.0.3"
val ScalatestVersion = "3.2.6"

//Swagger
val SwaggerAkkaHttpVersion = "2.4.0"
val SwaggerVersion = "2.1.7"

libraryDependencies ++= Seq(
  "javax.ws.rs" % "javax.ws.rs-api" % "2.1.1",
  "com.github.swagger-akka-http" %% "swagger-akka-http" % SwaggerAkkaHttpVersion,
  "io.swagger.core.v3" % "swagger-core" % SwaggerVersion,
  "io.swagger.core.v3" % "swagger-annotations" % SwaggerVersion,
  "io.swagger.core.v3" % "swagger-models" % SwaggerVersion,
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor-typed" % AkkaVersion,
  "com.typesafe.akka" %% "akka-stream" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http" % AkkaHttpVersion,
  "de.heikoseeberger" %% "akka-http-circe" % AkkaHttpCirceVersion,
)

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % ScalatestVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % AkkaVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % AkkaHttpVersion % Test,
)

libraryDependencies ++= Seq(
  "io.circe" %% "circe-core" % CirceVersion,
  "io.circe" %% "circe-generic" % CirceVersion,
  "io.circe" %% "circe-parser" % CirceVersion
)

libraryDependencies ++= Seq(
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.scala-logging" %% "scala-logging" % ScalaLoggingVersion
)

libraryDependencies += "org.hashids" % "hashids" % HashidsScalaVersion


docker / dockerfile := {
  val artifact: File = assembly.value
  val artifactTargetPath = s"/app/${artifact.name}"

  new Dockerfile {
    from("openjdk:8-jre")
    add(artifact, artifactTargetPath)
    entryPoint("java", "-jar", artifactTargetPath)
  }
}