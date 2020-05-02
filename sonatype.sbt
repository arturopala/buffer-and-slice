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

releaseCrossBuild := true
releaseUseGlobalVersion := true

usePgpKeyHex("3FB5C97965AFEFFE50E462D2C054F59D2084B5BA")

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
  releaseStepCommand("sonatypeBundleRelease"),
  setNextVersion,
  commitNextVersion,
  pushChanges
)