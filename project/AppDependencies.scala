import play.sbt.PlayImport.ws
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val dependencies = Seq(
    ws,
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-28" % "7.19.0",
    "uk.gov.hmrc"            %% "domain"                     % "8.3.0-play-28",
    "uk.gov.hmrc"            %% "play-frontend-hmrc"         % "7.13.0-play-28",
    "uk.gov.hmrc"            %% "play-partials"              % "8.4.0-play-28",
    "com.typesafe.play"      %% "play-json-joda"             % "2.9.2",
    "org.jsoup"              % "jsoup"                       % "1.14.3",
    "com.vladsch.flexmark"   % "flexmark-all"                % "0.35.10" % "test,it",
    "org.pegdown"            % "pegdown"                     % "1.6.0" % "test,it",
    "uk.gov.hmrc"            %% "bootstrap-test-play-28"     % "7.19.0" % Test,
    "org.scalatestplus"      %% "mockito-3-4"                % "3.2.1.0" % "test, it",
    "org.scalacheck"         %% "scalacheck"                 % "1.14.0" % "test, it",
    "org.mockito"            % "mockito-core"                % "4.1.0" % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"         % "5.1.0" % "test,it",
    "uk.gov.hmrc"            %% "service-integration-test"   % "1.3.0-play-28" % "test, it"
  )
}
