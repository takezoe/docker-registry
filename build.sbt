val ScalatraVersion = "2.7.1"
val JettyVersion = "9.4.35.v20201120"

ThisBuild / scalaVersion := "2.13.4"
ThisBuild / organization := "com.github.takezoe"

lazy val hello = (project in file("."))
  .settings(
    name := "docker-registry",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatra"      %% "scalatra"           % ScalatraVersion,
      "org.scalatra"      %% "scalatra-json"      % ScalatraVersion,
      "org.scalatra"      %% "scalatra-scalatest" % ScalatraVersion % "test",
      "org.json4s"        %% "json4s-jackson"     % "3.6.10",
      "ch.qos.logback"    %  "logback-classic"    % "1.2.3",
      "commons-io"        %  "commons-io"         % "2.11.0",
      "org.eclipse.jetty" %  "jetty-webapp"       % JettyVersion % "provided",
      "javax.servlet"     %  "javax.servlet-api"  % "3.1.0" % "provided",
    ),
  )

artifactName := { (v: ScalaVersion, m: ModuleID, a: Artifact) =>
  a.name + "." + a.extension
}

enablePlugins(JettyPlugin)
