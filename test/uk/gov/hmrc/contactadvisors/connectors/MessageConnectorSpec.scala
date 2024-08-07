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

package uk.gov.hmrc.contactadvisors.connectors

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock._
import com.github.tomakehurst.wiremock.core.WireMockConfiguration._
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status
import play.api.libs.json.Json
import uk.gov.hmrc.contactadvisors.domain.{ AdviceAlreadyExists, AdviceStored, StorageResult, UnexpectedError }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.utils.{ SecureMessageCreator, WithWiremock }

import javax.inject.{ Inject, Singleton }
import scala.concurrent.ExecutionContext

@Singleton
class TestMessageConnector @Inject() (
  http: HttpClientV2,
  servicesConfig: ServicesConfig
)(implicit ec: ExecutionContext)
    extends MessageConnector(http, servicesConfig)(ec) {

  override lazy val serviceUrl: String = s"http://localhost:58008"
}

class MessageConnectorSpec()
    extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with WithWiremock with TableDrivenPropertyChecks
    with IntegrationPatience {

  val messagePort = 58008
  override lazy val wireMockServer = new WireMockServer(wireMockConfig().port(messagePort))

  override def beforeAll() = {
    super.beforeAll()
    wireMockServer.start()
    WireMock.configureFor(messagePort)
  }

  override def beforeEach() = {
    super.beforeEach()
    wireMockServer.resetMappings()
    wireMockServer.resetRequests()
  }

  override def afterAll() = {
    super.afterAll()
    wireMockServer.stop()
  }

  "message connector" should {
    "return the message id from the response" in new TestCase {
      givenThat(
        post(urlEqualTo(expectedPath)).willReturn(
          aResponse().withStatus(Status.CREATED).withBody("""{"id":"12341234"}""")
        )
      )

      connector.create(secureMessage).futureValue must be(AdviceStored("12341234"))
    }

    "return MessageAlreadyExists failure with true when the message service returns 409 (conflict) while saving" in
      new TestCase {
        stubFor(post(urlEqualTo(expectedPath)).willReturn(aResponse().withStatus(Status.CONFLICT)))
        connector.create(secureMessage).futureValue must be(AdviceAlreadyExists)

      }

    forAll(Table("statusCode", 400, 401, 404, 415, 500)) { statusCode =>
      s"return Failure with reason for status=$statusCode" in new TestCase {

        val errorMessage = Json.obj("reason" -> "something went wrong")
        givenThat(
          post(urlEqualTo(expectedPath))
            .willReturn(
              aResponse()
                .withStatus(statusCode)
                .withBody(errorMessage.toString())
            )
        )

        val response: StorageResult = connector.create(secureMessage).futureValue
        response match {
          case UnexpectedError(reason) =>
            reason must include(expectedPath)
            reason must include(statusCode.toString)
            reason must include("'{\"reason\":\"something went wrong\"}'")
          case _ => fail("Unexpected storage result")
        }
      }
    }

    "fail when an IOException occurs when saving" in new TestCase {
      givenThat(
        post(urlEqualTo(expectedPath))
          .willReturn(
            aResponse()
              .withFault(Fault.RANDOM_DATA_THEN_CLOSE)
          )
      )

      val response = connector.create(secureMessage).futureValue
      response must be(UnexpectedError("Remotely closed"))
    }
  }

  trait TestCase {
    // val messageServiceBaseUrl = s"http://localhost:$messagePort"
    val expectedPath = s"/messages"

    val secureMessage = SecureMessageCreator.message
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val connector = app.injector.instanceOf[TestMessageConnector]
  }
}
