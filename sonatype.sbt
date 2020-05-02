// To sync with Maven central, you need to supply the following information:
sonatypeProfileName := "com.github.arturopala"

pomExtra in Global := {
  <url>github.com/arturopala/buffer-and-slice</url>
  <developers>
    <developer>
      <id>arturopala</id>
      <name>Artur Opala</name>
      <url>https://pl.linkedin.com/in/arturopala</url>
    </developer>
  </developers>
}

import ReleaseTransformations._

releaseCrossBuild := false
releaseUseGlobalVersion := true

releaseProcess := Seq[ReleaseStep](
  checkSnapshotDependencies,
  inquireVersions,
  runClean,
  releaseStepCommandAndRemaining("+test"),
  setReleaseVersion,
  releaseStepCommandAndRemaining("docs/mdoc"),
  commitReleaseVersion,
  tagRelease,
  releaseStepCommandAndRemaining("+publishSigned"),
  setNextVersion,
  commitNextVersion,
  releaseStepCommand("sonatypeBundleRelease"),
  pushChanges
)