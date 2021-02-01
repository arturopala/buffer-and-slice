val scala213 = "2.13.4"
val scala212 = "2.12.13"
val scala211 = "2.11.12"
val dottyNext = "3.0.0-M3"
val dottyStable = "3.0.0-M2"
val scalaJSVersion = "1.4.0"
val scalaNativeVersion = "0.4.0"
val mUnitVersion = "0.7.21"

val scala2Versions = List(scala213, scala212, scala211)
val scala3Versions = List(dottyNext, dottyStable)
val allScalaVersions = scala2Versions ++ scala3Versions

ThisBuild / scalaVersion := scala213

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
  releaseVersionBump := sbtrelease.Version.Bump.Major,
  publishTo := sonatypePublishToBundle.value,
  git.remoteRepo := "git@github.com:arturopala/buffer-and-slice.git",
  testFrameworks += new TestFramework("munit.Framework"),
  logBuffered := false,
  scalacOptions in (Compile, doc) ++= Seq(
    "-groups"
  ),
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
  crossScalaVersions := allScalaVersions,
  scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)),
  libraryDependencies ++= List(
    ("org.scala-js" %% "scalajs-test-interface" % scalaJSVersion % Test)
      .withDottyCompat(scalaVersion.value),
    ("org.scala-js" %% "scalajs-junit-test-runtime" % scalaJSVersion % Test)
      .withDottyCompat(scalaVersion.value)
  )
)

lazy val nativeSettings = List(
  scalaVersion := scala213,
  crossScalaVersions := Nil,
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
    mdocIn := baseDirectory.in(rootJVM).value / ".." / "src" / "docs",
    mdocOut := baseDirectory.in(rootJVM).value / "..",
    mdocVariables := Map(
      "VERSION"                  -> version.in(rootJVM).value,
      "SCALA_NATIVE_VERSION"     -> scalaNativeVersion,
      "SCALA_JS_VERSION"         -> scalaJSVersion,
      "DOTTY_NEXT_VERSION"       -> dottyNext,
      "DOTTY_STABLE_VERSION"     -> dottyStable,
      "SUPPORTED_SCALA_VERSIONS" -> allScalaVersions.map(v => s"`$v`").mkString(", ")
    ),
    skip in publish := true
  )
  .enablePlugins(MdocPlugin)

addCompilerPlugin("org.scala-native" % "junit-plugin" % nativeVersion cross CrossVersion.full)
