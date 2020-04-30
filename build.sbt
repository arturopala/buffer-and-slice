ThisBuild / name := "buffer-and-slice"
ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "com.github.arturopala"
ThisBuild / organizationName := "Artur Opala"
ThisBuild / startYear := Some(2020)
ThisBuild / licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt"))
ThisBuild / libraryDependencies += "org.scalameta" %%% "munit" % "0.7.4" % Test

val scala213 = "2.13.2"
val scala212 = "2.12.11"
val scala211 = "2.11.12"
val dottyNext = "0.24.0-RC1"
val dottyStable = "0.23.0"
val scalaJSVersion = "1.0.1"
val scalaNativeVersion = "0.4.0-M2"

val scala2Versions = List(scala213, scala212, scala211)
val scala3Versions = List(dottyNext, dottyStable)
val allScalaVersions = scala2Versions ++ scala3Versions

val sharedJVMSettings = List(
  crossScalaVersions := allScalaVersions
)

val sharedJSSettings = List(
  crossScalaVersions := scala2Versions,
  scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule))
)

val sharedNativeSettings = List(
  scalaVersion := scala211,
  crossScalaVersions := List(scala211)
)

val sharedSettings = Seq(
  name := "buffer-and-slice",
  excludeFilter in (Compile, unmanagedResources) := NothingFilter,
  scalafmtOnCompile in Compile := true,
  scalafmtOnCompile in Test := true,
  releaseVersionBump := sbtrelease.Version.Bump.Minor,
  publishTo := sonatypePublishTo.value,
  git.remoteRepo := "git@github.com:arturopala/buffer-and-slice.git",
  testFrameworks += new TestFramework("munit.Framework"),
  logBuffered := false,
  parallelExecution in Test := false
)

lazy val root = crossProject(JSPlatform, JVMPlatform/*, NativePlatform*/)
    .withoutSuffixFor(JVMPlatform)
    .crossType(CrossType.Pure)
    .in(file("."))
    .enablePlugins(AutomateHeaderPlugin, GhpagesPlugin, SiteScaladocPlugin)
    .settings(sharedSettings)
    .jvmSettings(
      sharedJVMSettings
    )
    .jsSettings(
      sharedJSSettings,
      libraryDependencies ++= List(
        "org.scala-js" %% "scalajs-test-interface" % scalaJSVersion,
        "org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion
      )
    )
    /*.nativeSettings(
      sharedNativeSettings,
      libraryDependencies ++= List(
        "org.scala-native" %%% "test-interface" % scalaNativeVersion
      )
    )*/

lazy val rootJVM = root.jvm
lazy val rootJS = root.js
//lazy val rootNative = root.native

lazy val docs = project
  .in(file("project-mdoc"))
  .dependsOn(rootJVM)
  .settings(
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