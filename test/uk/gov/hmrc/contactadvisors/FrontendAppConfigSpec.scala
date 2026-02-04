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

import org.mockito.Mockito.when
import play.api.{ Configuration, Environment }
import uk.gov.hmrc.utils.SpecBase

import org.mockito.ArgumentMatchers.any

class FrontendAppConfigSpec extends SpecBase {

  "analyticsToken" should {
    "return the correct value" in new Setup {
      appConfig.analyticsToken must be("N/A")
    }
  }

  "analyticsHost" should {
    "return the correct value" in new Setup {
      appConfig.analyticsHost must be("auto")
    }
  }

  "reportAProblemPartialUrl" should {
    "return the correct value" in new Setup {
      appConfig.reportAProblemPartialUrl must be("unused")
    }
  }

  "reportAProblemNonJSUrl" should {
    "return the correct value" in new Setup {
      appConfig.reportAProblemNonJSUrl must be("unused")
    }
  }

  "loadConfig" should {
    "throw exception for missing key" ignore {
      val config = mock[Configuration]
      val environment = mock[Environment]

      val appConfig: FrontendAppConfig = new FrontendAppConfig(config, environment)

      when(config.getOptional(any)(any)).thenReturn(None)

      intercept[Exception] {
        appConfig.analyticsHost
      }.getMessage mustBe "Missing configuration key:"
    }
  }

  trait Setup {
    val appConfig: FrontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
  }
}
