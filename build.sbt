val scala213 = "2.13.4"
val scala212 = "2.12.13"
val scala211 = "2.11.12"
val dottyNext = "3.0.0-RC3"
val dottyStable = "3.0.0-RC2"
val scalaJSVersion = "1.5.1"
val scalaNativeVersion = "0.4.0"
val mUnitVersion = "0.7.25"

val scala2Versions = List(scala213, scala212, scala211)
val scala3Versions = List(dottyNext, dottyStable)
val allScalaVersions = scala2Versions ++ scala3Versions

inThisBuild(
  List(
    scalaVersion := scala213,
    organization := "com.github.arturopala",
    homepage := Some(url("https://github.com/arturopala/buffer-and-slice")),
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer(
        "arturopala",
        "Artur Opala",
        "opala.artur@gmail.com",
        url("https://uk.linkedin.com/in/arturopala")
      )
    ),
    organizationName := "Artur Opala",
    startYear := Some(2020),
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.5.0",
    semanticdbEnabled := true,
    semanticdbVersion := scalafixSemanticdb.revision,
    scalafixScalaBinaryVersion := "2.13"
  )
)

lazy val sharedSettings = Seq(
  name := "buffer-and-slice",
  scalaVersion := scala213,
  Compile / excludeFilter := NothingFilter,
  unmanagedResources / excludeFilter := NothingFilter,
  (Compile / scalafmtOnCompile) := true,
  (Test / scalafmtOnCompile) := true,
  logBuffered := false,
  doc / scalacOptions += "-groups",
  scalacOptions.withRank(KeyRanks.Invisible) += "-Ywarn-unused", // required by `RemoveUnused` rule
  (Test / parallelExecution) := false,
  libraryDependencies += "org.scalameta" %%% "munit" % mUnitVersion % Test,
  headerLicense := Some(HeaderLicense.ALv2("2020", "Artur Opala"))
)

publish / skip := true
crossScalaVersions := List()
libraryDependencies += "org.scalameta" %%% "munit" % mUnitVersion % Test

lazy val jVMSettings = List(
  crossScalaVersions := allScalaVersions,
  gitHubPagesOrgName := "arturopala",
  gitHubPagesRepoName := "buffer-and-slice",
  gitHubPagesSiteDir := baseDirectory.value / "target" / "site"
)

lazy val jSSettings = List(
  crossScalaVersions := allScalaVersions,
  scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
  libraryDependencies ++= List(
    ("org.scala-js" %% "scalajs-test-interface" % scalaJSVersion % Test)
      .cross(CrossVersion.for3Use2_13),
    ("org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion % Test)
      .cross(CrossVersion.for3Use2_13)
  )
)

lazy val nativeSettings = List(
  scalaVersion := scala213,
  crossScalaVersions := scala2Versions,
  libraryDependencies ++= List(
    "org.scala-native" %%% "test-interface" % scalaNativeVersion % Test,
    "org.scala-native" %%% "junit-runtime"  % nativeVersion      % Test
  )
)

lazy val root = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("."))
  .settings(sharedSettings)
  .jvmSettings(jVMSettings)
  .jsSettings(jSSettings)
  .nativeSettings(nativeSettings)
  .jvmConfigure(
    _.enablePlugins(AutomateHeaderPlugin, GitHubPagesPlugin, SiteScaladocPlugin)
  )

lazy val rootJVM = root.jvm
lazy val rootJS = root.js
lazy val rootNative = root.native

lazy val docs = project
  .in(file("project-mdoc"))
  .dependsOn(rootJVM)
  .settings(
    sharedSettings,
    mdocIn := (rootJVM / baseDirectory).value / ".." / "src" / "docs",
    mdocOut := (rootJVM / baseDirectory).value / "..",
    mdocVariables := Map(
      "VERSION"                  -> previousStableVersion.value.getOrElse("0.1.0"),
      "SCALA_NATIVE_VERSION"     -> scalaNativeVersion,
      "SCALA_JS_VERSION"         -> scalaJSVersion,
      "DOTTY_NEXT_VERSION"       -> dottyNext,
      "DOTTY_STABLE_VERSION"     -> dottyStable,
      "SUPPORTED_SCALA_VERSIONS" -> allScalaVersions.map(v => s"`$v`").mkString(", ")
    ),
    publish / skip := true
  )
  .enablePlugins(MdocPlugin)

addCompilerPlugin("org.scala-native" % "junit-plugin" % nativeVersion cross CrossVersion.full)
