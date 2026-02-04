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

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping

import javax.inject.{ Inject, Singleton }
import org.scalatest.Inside.*
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatest.prop.TableDrivenPropertyChecks
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.http.Status.{ BAD_REQUEST, FORBIDDEN, INTERNAL_SERVER_ERROR, NOT_FOUND, OK, UNAUTHORIZED, UNSUPPORTED_MEDIA_TYPE }
import play.api.libs.json.{ JsObject, JsResultException, Json }
import play.api.{ Configuration, Environment }
import uk.gov.hmrc.contactadvisors.domain.UnexpectedFailure
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig
import uk.gov.hmrc.utils.WithWiremock

import scala.concurrent.ExecutionContext

@Singleton
class TestEntityResolverConnector @Inject() (
  http: HttpClientV2,
  val runModeConfiguration: Configuration,
  servicesConfig: ServicesConfig,
  val environment: Environment
)(implicit ec: ExecutionContext)
    extends EntityResolverConnector(http, servicesConfig)(ec) {
  override lazy val serviceUrl: String = s"http://localhost:8015"
}

class EntityResolverConnectorSpec
    extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with WithWiremock with TableDrivenPropertyChecks
    with IntegrationPatience {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  override lazy val dependenciesPort = 8015

  "The Entity Resolver connector" should {

    "return true when provided a tax identifier for a valid user, opted-in for paperless" in new TestCase {
      entityResolverReturns(
        OK,
        Some(
          Json.obj(
            "digital" -> true,
            "email" -> Json.obj(
              "email"       -> "bbc14eef-97d3-435e-975a-f2ab069af000@TEST.com",
              "mailboxFull" -> false,
              "status"      -> "verified"
            )
          )
        )
      )
      connector.validPaperlessUserWith(utr).futureValue must be(Some(PaperlessPreference(true)))
    }

    "return CustomerCannotReceiveAlerts when provided a tax identifier for a user that has opted out of paperless" in new TestCase {
      entityResolverReturns(OK, Some(Json.obj("digital" -> false)))

      connector.validPaperlessUserWith(utr).futureValue must be(Some(PaperlessPreference(false)))
    }

    "return UnknownTaxId when provided a tax identifier that cannot be resolved" in new TestCase {
      entityResolverReturns(NOT_FOUND)

      connector.validPaperlessUserWith(utr).futureValue must be(None)
    }

    forAll(Table("statusCode", BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, UNSUPPORTED_MEDIA_TYPE, INTERNAL_SERVER_ERROR)) {
      statusCode =>
        s"return unexpected failure when the response has status $statusCode" in new TestCase {
          entityResolverReturns(statusCode)

          inside(connector.validPaperlessUserWith(utr).failed.futureValue) { case UnexpectedFailure(msg) =>
            msg must include(statusCode.toString)
          }
        }
    }
  }

  "PaperlessPreference.formats" should {
    import PaperlessPreference.formats

    "read the json correctly" in new TestCase {
      Json.parse(paperlessPreferenceJsonString).as[PaperlessPreference] mustBe paperlessPreference
    }

    "throw exception for invalid json" in new TestCase {
      intercept[JsResultException] {
        Json.parse(paperlessPreferenceInvalidJsonString).as[PaperlessPreference]
      }
    }

    "write the object correctly" in new TestCase {
      Json.toJson(paperlessPreference) mustBe Json.parse(paperlessPreferenceJsonString)
    }
  }

  trait TestCase {
    val paperlessPreference: PaperlessPreference = PaperlessPreference(true)
    val paperlessPreferenceJsonString: String = """{"digital":true}""".stripMargin
    val paperlessPreferenceInvalidJsonString: String = """{}""".stripMargin

    def utr: SaUtr = SaUtr("0329u490uwesakdjf")

    def pathToPreferences: String = s"/portal/preferences/sa/$utr"

    def connector: TestEntityResolverConnector = app.injector.instanceOf(classOf[TestEntityResolverConnector])

    def entityResolverReturns(status: Int, responseBody: Option[JsObject] = None): StubMapping =
      givenThat(
        get(urlEqualTo(pathToPreferences)).willReturn(
          responseBody.fold(aResponse().withStatus(status).withBody("")) { json =>
            aResponse().withStatus(status).withBody(Json.stringify(json))
          }
        )
      )
  }

}
