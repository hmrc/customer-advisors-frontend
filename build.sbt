/*
 * Copyright 2026 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import uk.gov.hmrc.DefaultBuildSettings.{ defaultSettings, scalaSettings }

val appName = "customer-advisors-frontend"

Global / majorVersion := 2
Global / scalaVersion := "3.3.6"
Global / lintUnusedKeysOnLoad := false

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtDistributablesPlugin)
  .settings(scalaSettings *)
  .settings(defaultSettings() *)
  .settings(
    PlayKeys.playDefaultPort := 9848,
    libraryDependencies ++= AppDependencies.dependencies,
    Test / parallelExecution := false,
    Test / fork := false,
    Test / retrieveManaged := true,
    update / evictionWarningOptions := EvictionWarningOptions.default.withWarnScalaVersionEviction(false),
    routesGenerator := InjectedRoutesGenerator,
    scalacOptions := scalacOptions.value.diff(Seq("-Wunused:all")),
    scalacOptions ++= Seq(
      "-Wconf:src=routes/.*:s",
      "-Wconf:msg=Flag.*repeatedly:s",
      "-Wconf:src=views/.*:s"
    )
  )
  .settings(ScoverageSettings())

lazy val it = (project in file("it"))
  .enablePlugins(PlayScala)
  .dependsOn(microservice % "test->test")
  .settings(scalacOptions := scalacOptions.value.distinct)

Test / test := (Test / test)
  .dependsOn(scalafmtCheckAll)
  .value

it / test := (it / Test / test)
  .dependsOn(scalafmtCheckAll, it / scalafmtCheckAll)
  .value
