/*
 * Copyright 2023 HM Revenue & Customs
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

package uk.gov.hmrc.contactadvisors

import javax.inject.{ Inject, Singleton }
import play.api.{ Configuration, Environment }

trait AppConfig {
  val analyticsToken: String
  val analyticsHost: String
  val reportAProblemPartialUrl: String
  val reportAProblemNonJSUrl: String
}

@Singleton
class FrontendAppConfig @Inject() (val runModeConfiguration: Configuration, val environment: Environment)
    extends AppConfig {

  private def loadConfig(key: String) =
    runModeConfiguration.getOptional[String](key).getOrElse(throw new Exception(s"Missing configuration key: $key"))

  override val analyticsToken = loadConfig(s"google-analytics.token")
  override val analyticsHost = loadConfig(s"google-analytics.host")
  override val reportAProblemPartialUrl: String = "unused"
  override val reportAProblemNonJSUrl: String = "unused"
}
