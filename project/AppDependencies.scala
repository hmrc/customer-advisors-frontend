import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val bootstrap_version = "10.2.0"
  val silencerVersion = "1.7.16"

  val dependencies: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrap_version,
    "uk.gov.hmrc" %% "domain-play-30"             % "12.1.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "12.17.0",
    "org.jsoup"    % "jsoup"                      % "1.17.2",
    compilerPlugin(
      "com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.for3Use2_13With("", ".12")
    ),
    "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.for3Use2_13With("", ".12"),
    "uk.gov.hmrc" %% "bootstrap-test-play-30" % bootstrap_version % Test
  )
}
