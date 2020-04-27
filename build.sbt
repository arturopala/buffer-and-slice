ThisBuild / scalaVersion := "2.13.1"
ThisBuild / organization := "com.github.arturopala"
ThisBuild / organizationName := "Artur Opala"
ThisBuild / startYear := Some(2020)

lazy val supportedScalaVersions = List("0.23.0-RC1","2.13.2", "2.12.11", "2.11.12")

lazy val Benchmark = config("benchmark") extend Test

lazy val root = (project in file("."))
  .enablePlugins(AutomateHeaderPlugin, GhpagesPlugin, SiteScaladocPlugin)
  .settings(
    name := "buffer-and-slice",
    licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
    libraryDependencies ++= dependencies(scalaVersion.value),
    crossScalaVersions := supportedScalaVersions,
    excludeFilter in (Compile, unmanagedResources) := NothingFilter,
    scalafmtOnCompile in Compile := true,
    scalafmtOnCompile in Test := true,
    releaseVersionBump := sbtrelease.Version.Bump.Minor,
    publishTo := sonatypePublishTo.value,
    scalacOptions in (Compile, doc) ++= Seq(
      "-groups"
    ),
    git.remoteRepo := "git@github.com:arturopala/buffer-and-slice.git",
    testFrameworks += new TestFramework("munit.Framework"),
    testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework"),
    logBuffered := false,
    parallelExecution in Test := false,
    parallelExecution in Benchmark := false,
  )
  .configs(Benchmark)
  .settings(
    inConfig(Benchmark)(Defaults.testSettings): _*
  )

def dependencies(scalaVersion: String) = {
  CrossVersion.partialVersion(scalaVersion) match {
    case Some((2, _)) => List(
      "org.scalameta" %% "munit" % "0.7.3" % Test,
      "com.storm-enroute" %% "scalameter" % "0.19"  % Test
    )
    case Some((0, _)) => List(
      "org.scalameta" %% "munit" % "0.7.3" % Test
    )
  }
}
