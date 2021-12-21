
import play.sbt.PlayImport.ws
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val dependencies = Seq(
    ws,
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-28" % "5.18.0",
    "uk.gov.hmrc"            %% "auth-client"              % "5.7.0-play-28",
    "uk.gov.hmrc"            %% "govuk-template"           % "5.72.0-play-28",
    "uk.gov.hmrc"            %% "domain"                   % "6.2.0-play-28",
    "uk.gov.hmrc"            %% "play-frontend-hmrc"       % "1.31.0-play-28",
    "uk.gov.hmrc"            %% "play-partials"            % "8.2.0-play-28",
    "com.typesafe.play"      %% "play-json-joda"           % "2.9.2",
    "org.jsoup"              % "jsoup"                     % "1.14.3",
    "org.skyscreamer"        % "jsonassert"                % "1.5.0",
    "com.github.tomakehurst" % "wiremock-jre8"             % "2.23.0" % "test,it",
    "org.pegdown"            % "pegdown"                   % "1.6.0" % "test,it",
    "org.mockito"            % "mockito-all"               % "1.10.19" % "test",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0" % "test,it",
    "uk.gov.hmrc"            %% "service-integration-test" % "1.2.0-play-28" % "test, it"

  )


}
