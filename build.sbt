
import uk.gov.hmrc.DefaultBuildSettings.{addTestReportOption, defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.{DefaultBuildSettings, ExternalService}
import uk.gov.hmrc.ForkedJvmPerTestSettings.oneForkedJvmPerTest
import uk.gov.hmrc.ServiceManagerPlugin.Keys.itDependenciesList
import uk.gov.hmrc.sbtdistributables.SbtDistributablesPlugin.publishingSettings


val appName = "customer-advisors-frontend"


val silencerVersion = "1.7.0"

lazy val externalServices = List(
 ExternalService("AUTH"),
 ExternalService("USER_DETAILS"),
 ExternalService("PREFERENCES"),
 ExternalService("MESSAGE"),
 ExternalService("ENTITY_RESOLVER"),
 ExternalService("HMRCDESKPRO"),
 ExternalService("SA"),
 ExternalService("DATASTREAM", enableTestOnlyEndpoints = true)
)


lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(majorVersion := 1)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin, BuildInfoPlugin)
  .settings(scalaSettings: _*)
  .settings(publishingSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
   targetJvm := "jvm-1.8",
   scalaVersion := "2.12.12",
   libraryDependencies ++= AppDependencies.dependencies,
   parallelExecution in Test := false,
   fork in Test := false,
   retrieveManaged := true,
   evictionWarningOptions in update := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
   routesGenerator := InjectedRoutesGenerator,
   scalacOptions ++= List(
    "-feature",
    "-language:postfixOps",
    "-language:reflectiveCalls",
    "-Xlint:-missing-interpolator"
   )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(ServiceManagerPlugin.serviceManagerSettings)
  .settings(itDependenciesList := externalServices)
  .settings(
    DefaultBuildSettings.integrationTestSettings()
  )
  .settings(resolvers ++= Seq(Resolver.jcenterRepo))
  .settings(ScoverageSettings())
  