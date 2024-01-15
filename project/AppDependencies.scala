import play.sbt.PlayImport.ws
import play.core.PlayVersion.current
import sbt._

object AppDependencies {

  val dependencies = Seq(
    ws,
    "uk.gov.hmrc"            %% "bootstrap-frontend-play-30" % "8.4.0",
    "uk.gov.hmrc"            %% "domain-play-30"             % "9.0.0",
    "uk.gov.hmrc"            %% "play-frontend-hmrc-play-30" % "8.3.0",
    "uk.gov.hmrc"            %% "play-partials-play-30"      % "9.1.0",
    "com.typesafe.play"      %% "play-json-joda"             % "2.9.2",
    "org.jsoup"              %  "jsoup"                      % "1.17.2",
    "com.vladsch.flexmark"   %  "flexmark-all"               % "0.35.10" % Test,
    "org.pegdown"            %  "pegdown"                    % "1.6.0"   % Test,
    "uk.gov.hmrc"            %% "bootstrap-test-play-30"     % "8.4.0"  % Test,
    "org.scalatestplus"      %% "mockito-3-4"                % "3.2.1.0" % Test,
    "org.scalacheck"         %% "scalacheck"                 % "1.14.0"  % Test,
    "org.mockito"            %  "mockito-core"               % "4.1.0"   % Test,
    "org.scalatestplus.play" %% "scalatestplus-play"         % "5.1.0"   % Test
  )
}
