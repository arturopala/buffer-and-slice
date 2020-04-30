val scala213 = "2.13.2"
val scala212 = "2.12.11"
val scala211 = "2.11.12"
val dottyNext = "0.24.0-RC1"
val dottyStable = "0.23.0"
val scalaJSVersion = "1.0.1"
val scalaNativeVersion = "0.4.0-M2"
val mUnitVersion = "0.7.4"

val scala2Versions = List(scala213, scala212, scala211)
val scala3Versions = List(dottyNext, dottyStable)
val allScalaVersions = scala2Versions ++ scala3Versions

lazy val sharedSettings = Seq(
  name := "buffer-and-slice",
  organization := "com.github.arturopala",
  organizationName := "Artur Opala",
  startYear := Some(2020),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  scalaVersion := scala213,
  excludeFilter in (Compile, unmanagedResources) := NothingFilter,
  scalafmtOnCompile in Compile := true,
  scalafmtOnCompile in Test := true,
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  publishTo := sonatypePublishTo.value,
  git.remoteRepo := "git@github.com:arturopala/buffer-and-slice.git",
  testFrameworks += new TestFramework("munit.Framework"),
  logBuffered := false,
  parallelExecution in Test := false,
  libraryDependencies += "org.scalameta" %%% "munit" % mUnitVersion % Test
)

skip in publish := true
crossScalaVersions := List()
libraryDependencies += "org.scalameta" %%% "munit" % mUnitVersion % Test

lazy val jVMSettings = List(
  crossScalaVersions := allScalaVersions
)

lazy val jSSettings = List(
  crossScalaVersions := scala2Versions,
  scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
  libraryDependencies ++= List(
    "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion % Test,
    "org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion % Test,
  )
)

lazy val nativeSettings = List(
  scalaVersion := scala211,
  crossScalaVersions := List(scala211),
  libraryDependencies ++= List(
    "org.scala-native" %%% "test-interface" % scalaNativeVersion % Test
  ),
  nativeLinkStubs in Test := true
)

lazy val root = crossProject(JSPlatform, JVMPlatform, NativePlatform)
    .crossType(CrossType.Pure)
    .in(file("."))
    .settings(sharedSettings)
    .jvmSettings(jVMSettings)
    .jsSettings(jSSettings)
    .nativeSettings(nativeSettings)
    .jvmConfigure(
      _.enablePlugins(AutomateHeaderPlugin, GhpagesPlugin, SiteScaladocPlugin)
    )


lazy val rootJVM = root.jvm
lazy val rootJS = root.js
lazy val rootNative = root.native

lazy val docs = project
  .in(file("project-mdoc"))
  .dependsOn(rootJVM)
  .settings(
    sharedSettings,
    mdocIn := baseDirectory.in(rootJVM).value  / ".." / "src" / "docs",
    mdocOut := baseDirectory.in(rootJVM).value / "..",
    mdocVariables := Map(
      "VERSION" -> version.in(rootJVM).value,
      "SCALA_NATIVE_VERSION" -> scalaNativeVersion,
      "SCALA_JS_VERSION" -> scalaJSVersion,
      "DOTTY_NEXT_VERSION" -> dottyNext,
      "DOTTY_STABLE_VERSION" -> dottyStable,
      "SUPPORTED_SCALA_VERSIONS" -> allScalaVersions.map(v => s"`$v`").mkString(", ")
    ),
    skip in publish := true
  )
  .enablePlugins(MdocPlugin)
