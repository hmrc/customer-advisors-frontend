import play.sbt.PlayImport.ws
import sbt._

object AppDependencies {

  val dependencies = Seq(
    ws,
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-30" % "9.0.0",
    "uk.gov.hmrc"            %% "domain-play-30"             % "10.0.0",
    "uk.gov.hmrc"            %% "play-frontend-hmrc-play-30" % "10.3.0",
    "org.jsoup"              % "jsoup"                       % "1.17.2",
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"     % "9.0.0" % Test,
    "org.scalatestplus" %% "mockito-4-11" % "3.2.17.0" % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"         % "7.0.0" % Test
  )
}
