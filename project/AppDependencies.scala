
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




    "com.vladsch.flexmark" % "flexmark-all" % "0.35.10",




    "org.pegdown"            % "pegdown"                   % "1.6.0" % "test,it",


    "uk.gov.hmrc"            %% "bootstrap-test-play-28"   % "5.7.0"          % Test,
    "org.scalatest"          %% "scalatest"                % "3.0.0"          % Test,
    "com.typesafe.play"      %% "play-test"                % current          % Test,
    "uk.gov.hmrc"            %% "service-integration-test" % "1.1.0-play-28"  % "test, it",

//    "org.mockito"            %% "mockito-scala"             % "1.16.46" % "test, it",
    "org.scalatestplus"      %% "mockito-3-4"               % "3.2.1.0" % "test, it",


//    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0"          % "test, it",
//    "com.github.tomakehurst" % "wiremock-standalone"        % "2.20.0" % "test, it",
    "org.scalacheck"         %% "scalacheck"                % "1.14.0" % "test, it",
    "org.mockito"            % "mockito-core"              % "4.1.0"         % "test, it",
    "org.scalatestplus.play" %% "scalatestplus-play"       % "5.1.0" % "test,it",
    "uk.gov.hmrc"            %% "service-integration-test" % "1.2.0-play-28" % "test, it"

  )


}
