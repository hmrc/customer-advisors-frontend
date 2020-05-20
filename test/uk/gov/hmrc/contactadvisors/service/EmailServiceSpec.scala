/*
 * Copyright 2020 HM Revenue & Customs
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

package uk.gov.hmrc.contactadvisors.service

import com.google.inject.AbstractModule
import net.codingwell.scalaguice.ScalaModule
import org.mockito.Matchers._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.BeforeAndAfter
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mockito.MockitoSugar
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import play.api.mvc.Results
import play.api.test.Helpers._
import uk.gov.hmrc.contactadvisors.connectors.EmailConnector
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.{ ExecutionContext, Future }

class EmailServiceSpec extends PlaySpec with MockitoSugar with ScalaFutures with GuiceOneAppPerSuite with BeforeAndAfter {
  implicit val hc = HeaderCarrier()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val mockEmailConnector: EmailConnector = mock[EmailConnector]
  val validClientId = "ba6219ed-d6be-48e1-8612-ed5e793274f7"
  val invalidClientId = "invalid-client-id"

  implicit lazy override val app: Application = new GuiceApplicationBuilder()
    .overrides(new AbstractModule with ScalaModule {
      override def configure(): Unit =
        bind[EmailConnector].toInstance(mockEmailConnector)
    })
    .configure("metrics.enabled" -> "false")
    .configure("email.clientIds" -> List(validClientId))
    .build()
  val emailService = app.injector.instanceOf[EmailService]

  before {
    Mockito.reset(mockEmailConnector)
  }

  "doSendEmail" should {

    "forward emailConnector's result on valid payload" in new TestCase {
      when(mockEmailConnector.send(any())(any())).thenReturn(Future.successful(Results.Accepted))
      val result = emailService.doSendEmail(validPayload)
      result.futureValue mustBe (Results.Accepted)
      verify(mockEmailConnector, times(1)).send(any())(any())
    }

    "return BadRequest on payload with extra parameters" in new TestCase {
      val result = emailService.doSendEmail(extraParameters)
      status(result) mustBe (Status.BAD_REQUEST)
      contentAsString(result) mustBe """{"error": "invalid parameters"}"""
      verify(mockEmailConnector, never).send(any())(any())
    }

    "return BadRequest on payload with empty parameters" in new TestCase {
      val result = emailService.doSendEmail(emptyParameters)
      status(result) mustBe (Status.BAD_REQUEST)
      contentAsString(result) mustBe """{"error": "invalid parameters"}"""
      verify(mockEmailConnector, never).send(any())(any())
    }

    "return BadRequest on payload with missing parameters" in new TestCase {
      val result = emailService.doSendEmail(missingParameters)
      status(result) mustBe (Status.BAD_REQUEST)
      contentAsString(result) mustBe """{"error": "invalid parameters"}"""
      verify(mockEmailConnector, never).send(any())(any())
    }

    "return FORBIDDEN on payload with invalid clientId" in new TestCase {
      val result = emailService.doSendEmail(invalidClientIdPayload)
      status(result) mustBe (Status.FORBIDDEN)
      contentAsString(result) mustBe """{"error": "invalid clientId"}"""
      verify(mockEmailConnector, never).send(any())(any())
    }

    "return FORBIDDEN on payload with missing clientId" in new TestCase {
      val result = emailService.doSendEmail(missingClientIdPayload)
      status(result) mustBe (Status.FORBIDDEN)
      contentAsString(result) mustBe """{"error": "invalid clientId"}"""
      verify(mockEmailConnector, never).send(any())(any())
    }
  }

  trait TestCase {
    val validPayload = Json.parse(s"""
                                     |{
                                     |  "to": ["example@domain.com"],
                                     |  "templateId": "seiss_claim_now",
                                     |  "parameters": {
                                     |        "name": "Mr Joe Bloggs"
                                     |  },
                                     |  "clientId": "$validClientId"
                                     |}
        """.stripMargin)

    val extraParameters = Json.parse(s"""
                                        |{
                                        |  "to": ["example@domain.com"],
                                        |  "templateId": "seiss_claim_now",
                                        |  "parameters": {
                                        |        "name": "Mr Joe Bloggs",
                                        |        "secret": "secret value"
                                        |  },
                                        |  "clientId": "$validClientId"
                                        |}
        """.stripMargin)

    val emptyParameters = Json.parse(s"""
                                        |{
                                        |  "to": ["example@domain.com"],
                                        |  "templateId": "seiss_claim_now",
                                        |  "parameters": {
                                        |  },
                                        |  "clientId": "$validClientId"
                                        |}
        """.stripMargin)

    val missingParameters = Json.parse(s"""
                                          |{
                                          |  "to": ["example@domain.com"],
                                          |  "templateId": "seiss_claim_now",
                                          |  "clientId": "$validClientId"
                                          |}
        """.stripMargin)
  }

  val invalidClientIdPayload = Json.parse(s"""
                                             |{
                                             |  "to": ["example@domain.com"],
                                             |  "templateId": "seiss_claim_now",
                                             |  "parameters": {
                                             |        "name": "Mr Joe Bloggs"
                                             |  },
                                             |  "clientId": "$invalidClientId"
                                             |}
        """.stripMargin)

  val missingClientIdPayload = Json.parse(s"""
                                             |{
                                             |  "to": ["example@domain.com"],
                                             |  "templateId": "seiss_claim_now",
                                             |  "parameters": {
                                             |        "name": "Mr Joe Bloggs"
                                             |  }
                                             |}
        """.stripMargin)

}
