import uk.gov.hmrc.DefaultBuildSettings.{ defaultSettings, scalaSettings }
import uk.gov.hmrc.DefaultBuildSettings

val appName = "customer-advisors-frontend"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(majorVersion := 1)
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    scalaVersion := "2.13.8",
    libraryDependencies ++= AppDependencies.dependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator,
    scalacOptions ++= List(
      "-feature",
      "-language:postfixOps",
      "-language:reflectiveCalls",
      "-Xlint:-missing-interpolator",
      "-Wconf:src=routes/.*:s",
      "-Wconf:src=html/.*:s"
    )
  )
  .configs(IntegrationTest)
  .settings(inConfig(IntegrationTest)(Defaults.itSettings): _*)
  .settings(
    DefaultBuildSettings.integrationTestSettings()
  )
  .settings(resolvers ++= Seq(Resolver.jcenterRepo))
  .settings(ScoverageSettings())
