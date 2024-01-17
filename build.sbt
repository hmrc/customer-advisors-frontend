import uk.gov.hmrc.DefaultBuildSettings.{defaultSettings, scalaSettings, targetJvm}
import uk.gov.hmrc.DefaultBuildSettings

val appName = "customer-advisors-frontend"

Global / majorVersion := 1
Global / scalaVersion := "2.13.12"

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(scalaSettings: _*)
  .settings(defaultSettings(): _*)
  .settings(
    targetJvm := "jvm-11",
    libraryDependencies ++= AppDependencies.dependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    Test / 
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
  .settings(
    Test / scalacOptions := Seq(
      "-Ywarn-value-discard"
    )
  )
  .settings(resolvers ++= Seq(Resolver.jcenterRepo))
  .settings(ScoverageSettings())


lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(
    Test / scalacOptions := Seq(
      "-Ywarn-value-discard"
    )
  )