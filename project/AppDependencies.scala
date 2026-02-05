import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val bootstrap_version = "10.5.0"

  val dependencies: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrap_version,
    "uk.gov.hmrc" %% "domain-play-30"             % "13.0.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % "12.29.0",
    "org.jsoup"    % "jsoup"                      % "1.17.2",
    "uk.gov.hmrc" %% "bootstrap-test-play-30"     % bootstrap_version % Test
  )
}
