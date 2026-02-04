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
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.*
import com.github.tomakehurst.wiremock.http.Fault
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.libs.json.{ JsObject, JsResultException, Json }
import uk.gov.hmrc.contactadvisors.connectors.models.SecureMessage
import uk.gov.hmrc.contactadvisors.domain.{ AdviceAlreadyExists, AdviceStored, StorageResult, UnexpectedError }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.utils.{ SecureMessageCreator, WithWiremock }
import play.api.http.Status.{ BAD_REQUEST, CONFLICT, CREATED, INTERNAL_SERVER_ERROR, NOT_FOUND, UNAUTHORIZED, UNSUPPORTED_MEDIA_TYPE }
import uk.gov.hmrc.utils.TestData.TEST_ID

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

  override def beforeAll(): Unit = {
    super.beforeAll()
    wireMockServer.start()
    WireMock.configureFor(messagePort)
  }

  override def beforeEach(): Unit = {
    super.beforeEach()
    wireMockServer.resetMappings()
    wireMockServer.resetRequests()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    wireMockServer.stop()
  }

  "message connector" should {
    "return the message id from the response" in new TestCase {
      givenThat(
        post(urlEqualTo(expectedPath)).willReturn(
          aResponse().withStatus(CREATED).withBody("""{"id":"12341234"}""")
        )
      )

      connector.create(secureMessage).futureValue must be(AdviceStored("12341234"))
    }

    "return MessageAlreadyExists failure with true when the message service returns 409 (conflict) while saving" in
      new TestCase {
        stubFor(post(urlEqualTo(expectedPath)).willReturn(aResponse().withStatus(CONFLICT)))
        connector.create(secureMessage).futureValue must be(AdviceAlreadyExists)

      }

    forAll(Table("statusCode", BAD_REQUEST, UNAUTHORIZED, NOT_FOUND, UNSUPPORTED_MEDIA_TYPE, INTERNAL_SERVER_ERROR)) {
      statusCode =>
        s"return Failure with reason for status=$statusCode" in new TestCase {

          val errorMessage: JsObject = Json.obj("reason" -> "something went wrong")
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

      val response: StorageResult = connector.create(secureMessage).futureValue
      response must be(UnexpectedError("Remotely closed"))
    }
  }

  "MessageResponse.formats" should {
    import MessageResponse.formats

    "read the json correctly" in new TestCase {
      Json.parse(messageResponseJsonString).as[MessageResponse] mustBe messageResponse
    }

    "throw exception for invalid json" in new TestCase {
      intercept[JsResultException] {
        Json.parse(messageResponseInvalidJsonString).as[MessageResponse]
      }
    }

    "write the object correctly" in new TestCase {
      Json.toJson(messageResponse) mustBe Json.parse(messageResponseJsonString)
    }
  }

  trait TestCase {
    val expectedPath = s"/messages"
    val messageResponse: MessageResponse = MessageResponse(TEST_ID)
    val messageResponseJsonString: String = """{"id":"test_id"}""".stripMargin
    val messageResponseInvalidJsonString: String = """{}""".stripMargin

    val secureMessage: SecureMessage = SecureMessageCreator.message
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val connector: TestMessageConnector = app.injector.instanceOf[TestMessageConnector]
  }
}
