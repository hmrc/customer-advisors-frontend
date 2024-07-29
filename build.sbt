import uk.gov.hmrc.DefaultBuildSettings.{ defaultSettings, scalaSettings }

val appName = "customer-advisors-frontend"

Global / majorVersion := 2
Global / scalaVersion := "3.4.2"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    libraryDependencies ++= AppDependencies.dependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    Test / retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator
  )
  .settings(resolvers ++= Seq(Resolver.jcenterRepo))
  .settings(ScoverageSettings())

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
