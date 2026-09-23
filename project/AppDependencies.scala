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

import play.sbt.PlayImport.ws
import sbt.*

object AppDependencies {

  val bootstrap_version = "10.7.0"
  val playFrontendHmrcVersion = "13.14.0"

  val dependencies: Seq[ModuleID] = Seq(
    ws,
    "uk.gov.hmrc" %% "bootstrap-frontend-play-30" % bootstrap_version,
    "uk.gov.hmrc" %% "domain-play-30"             % "13.0.0",
    "uk.gov.hmrc" %% "play-frontend-hmrc-play-30" % playFrontendHmrcVersion,
    "org.jsoup"    % "jsoup"                      % "1.17.2",
    "uk.gov.hmrc" %% "bootstrap-test-play-30"     % bootstrap_version % Test
  )
}
