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
      "org.eclipse.jetty" %  "jetty-webapp"       % JettyVersion % "provided",
      "javax.servlet"     %  "javax.servlet-api"  % "3.1.0" % "provided",
    ),
  )

// Packaging options
packageOptions += Package.MainClass("JettyLauncher")

// Create executable war file
val ExecutableConfig = config("executable").hide
Keys.ivyConfigurations += ExecutableConfig
libraryDependencies ++= Seq(
  "org.eclipse.jetty" % "jetty-security"     % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-webapp"       % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-continuation" % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-server"       % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-xml"          % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-http"         % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-servlet"      % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-io"           % JettyVersion % "executable",
  "org.eclipse.jetty" % "jetty-util"         % JettyVersion % "executable"
)

val executableKey = TaskKey[File]("executable")
executableKey := {
  import java.util.jar.Attributes.{Name => AttrName}
  import java.util.jar.{Manifest => JarManifest}

  val workDir = Keys.target.value / "executable"
  val warName = Keys.name.value + ".war"

  val log = streams.value.log
  log info s"building executable webapp in ${workDir}"

  // initialize temp directory
  val temp = workDir / "webapp"
  IO delete temp

  // include jetty classes
  val jettyJars = Keys.update.value select configurationFilter(name = ExecutableConfig.name)
  jettyJars foreach { jar =>
    IO unzip (jar, temp, (name: String) =>
      (name startsWith "javax/") ||
        (name startsWith "org/"))
  }

  // include original war file
  val warFile = (Keys.`package`).value
  IO unzip (warFile, temp)

  // include launcher classes
  val classDir = (Compile / Keys.classDirectory).value
  val launchClasses = Seq("JettyLauncher.class")
  launchClasses foreach { name =>
    IO copyFile (classDir / name, temp / name)
  }

  // zip it up
  IO delete (temp / "META-INF" / "MANIFEST.MF")
  val contentMappings = (temp.allPaths --- PathFinder(temp)).get pair { file =>
    IO.relativizeFile(temp, file)
  }
  val manifest = new JarManifest
  manifest.getMainAttributes put (AttrName.MANIFEST_VERSION, "1.0")
  manifest.getMainAttributes put (AttrName.MAIN_CLASS, "JettyLauncher")
  val outputFile = workDir / warName
  IO jar (contentMappings.map { case (file, path) => (file, path.toString) }, outputFile, manifest, None)

  // done
  log info s"built executable webapp ${outputFile}"
  outputFile
}
enablePlugins(JettyPlugin)
