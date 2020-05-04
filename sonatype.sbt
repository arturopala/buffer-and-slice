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
releaseVersionBump := sbtrelease.Version.Bump.Major

usePgpKeyHex("D9267F3ECB3CF847330BA02AAAC19B29BEF3DCBF")

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