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

package uk.gov.hmrc.contactadvisors.controllers

import org.mockito.ArgumentCaptor
import org.scalatest.matchers.must.Matchers
import org.mockito.Mockito._
import org.scalatest.concurrent.{ Eventually, IntegrationPatience, ScalaFutures }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.i18n.MessagesApi
import play.api.mvc.{ MessagesControllerComponents, Result }
import play.api.test.FakeRequest
import play.api.{ Application, Configuration, Environment }
import uk.gov.hmrc.contactadvisors.FrontendAppConfig
import uk.gov.hmrc.contactadvisors.domain._
import uk.gov.hmrc.contactadvisors.service.SecureMessageService
import uk.gov.hmrc.contactadvisors.views.html.secureMessage.{ Duplicate, DuplicateV2, Inbox, InboxV2, Not_paperless, Success, SuccessV2, Unexpected, UnexpectedV2, Unknown }
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.EventKeys
import uk.gov.hmrc.play.audit.http.connector.{ AuditConnector, AuditResult }
import uk.gov.hmrc.play.audit.model.DataEvent
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{ verify, when }
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.http.Status.SEE_OTHER
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.SeeOther
import play.api.test.Helpers.status
import play.api.test.Helpers.defaultAwaitTimeout

import scala.concurrent.Future

class CustomerAdviceAuditV2Spec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite with IntegrationPatience with Eventually {

  implicit lazy override val app: Application = new GuiceApplicationBuilder()
    .configure(
      "auditing.enabled" -> true
    )
    .build()

  val appConfig = app.injector.instanceOf[FrontendAppConfig]
  val controllerComponents = app.injector.instanceOf[MessagesControllerComponents]
  val messageApi = app.injector.instanceOf[MessagesApi]
  val inboxPage = app.injector.instanceOf[Inbox]
  val inboxPageV2 = app.injector.instanceOf[InboxV2]
  val successPage = app.injector.instanceOf[Success]
  val successPageV2 = app.injector.instanceOf[SuccessV2]
  val duplicatePage = app.injector.instanceOf[Duplicate]
  val duplicatePageV2 = app.injector.instanceOf[DuplicateV2]
  val notPaperlessPage = app.injector.instanceOf[Not_paperless]
  val unknownPage = app.injector.instanceOf[Unknown]
  val unexpectedPage = app.injector.instanceOf[Unexpected]
  val unexpectedV2Page = app.injector.instanceOf[UnexpectedV2]

  "SecureMessageController" should {

    "audit the successful event" in new TestCaseV2 {
      when(secureMessageServiceMock.createMessageV2(any(), any())(any(), any()))
        .thenReturn(Future.successful(AdviceStored("1234")))

      when(secureMessageServiceMock.generateExternalRefID).thenReturn("75d80f37-2cb4-4571-a100-5f8511986fb7")

      val dataEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      val result: Result = controller.submitV2()(request).futureValue
      status(Future.successful(result)) must be(SEE_OTHER)

      verify(auditConnectorMock).sendEvent(dataEventCaptor.capture())(any(), any())

      val event = dataEventCaptor.getValue

      withClue("event auditSource") {
        event.auditSource must be("customer-advisors-frontend")
      }
      withClue("event auditType") {
        event.auditType must be("TxSucceeded")
      }
      withClue("event eventId") {
        event.eventId must include regex ("^[0-9a-fA-F-]+$")
      }
      withClue("event tags.transactionName") {
        event.tags.get(EventKeys.TransactionName) must not {
          be(None)
        }
        event.tags.get(EventKeys.TransactionName).get must be("Message Created")
      }

      withClue("event detail.messageId") {
        event.detail.get("messageId") must not {
          be(None)
        }
        event.detail.get("messageId").get must be("1234")
      }

      withClue("event details.externalRef") {
        event.detail.get("externalRef") must not {
          be(None)
        }
        event.detail.get("externalRef").get must include regex ("^[0-9a-fA-F-]+$")
      }

      withClue("event detail.fhddsRef") {
        event.detail.get("fhddsRef") must not {
          be(None)
        }
        event.detail.get("fhddsRef").get must include regex ("XZFH00000100024")
      }

      withClue("event detail.messageType") {
        event.detail.get("messageType") must not {
          be(None)
        }
        event.detail.get("messageType").get must be("fhddsAlertMessage")
      }
    }

    "audit the duplicate message event" in new TestCaseV2 {
      when(secureMessageServiceMock.createMessageV2(any(), any())(any(), any()))
        .thenReturn(Future.successful(AdviceAlreadyExists))
      when(secureMessageServiceMock.generateExternalRefID).thenReturn("75d80f37-2cb4-4571-a100-5f8511986fb7")

      val dataEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      val result = controller.submitV2()(request).futureValue
      status(Future.successful(result)) must be(SEE_OTHER)

      verify(auditConnectorMock).sendEvent(dataEventCaptor.capture())(any(), any())

      val event = dataEventCaptor.getValue

      withClue("event auditSource") {
        event.auditSource must be("customer-advisors-frontend")
      }
      withClue("event auditType") {
        event.auditType must be("TxSucceeded")
      }
      withClue("event eventId") {
        event.eventId must include regex ("^[0-9a-fA-F-]+$")
      }
      withClue("event tags.transactionName") {
        event.tags.get(EventKeys.TransactionName) must not {
          be(None)
        }
        event.tags.get(EventKeys.TransactionName).get must be("Message Duplicate Request")
      }

      withClue("event detail.messageId") {
        event.detail.get("messageId") must not {
          be(None)
        }
        event.detail.get("messageId").get must be("")
      }

      withClue("event detal.fhddsRef") {
        event.detail.get("fhddsRef") must not {
          be(None)
        }
        event.detail.get("fhddsRef").get must include regex ("XZFH00000100024")
      }

      withClue("event details.externalRef") {
        event.detail.get("externalRef") must not {
          be(None)
        }
        event.detail.get("externalRef").get must include regex ("^[0-9a-fA-F-]+$")
      }

      withClue("event detail.messageType") {
        event.detail.get("messageType") must not {
          be(None)
        }
        event.detail.get("messageType").get must be("fhddsAlertMessage")
      }

    }

    "audit the unexpected error event" in new TestCaseV2 {
      when(secureMessageServiceMock.createMessageV2(any(), any())(any(), any()))
        .thenReturn(Future.successful(UnexpectedError("this is the reason")))
      when(secureMessageServiceMock.generateExternalRefID).thenReturn("75d80f37-2cb4-4571-a100-5f8511986fb7")

      val dataEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      controller.submitV2()(request).futureValue

      verify(auditConnectorMock).sendEvent(dataEventCaptor.capture())(any(), any())

      val event = dataEventCaptor.getValue

      withClue("event auditSource") {
        event.auditSource must be("customer-advisors-frontend")
      }
      withClue("event auditType") {
        event.auditType must be("TxFailed")
      }
      withClue("event eventId") {
        event.eventId must include regex ("^[0-9a-fA-F-]+$")
      }
      withClue("event tags.transactionName") {
        event.tags.get(EventKeys.TransactionName) must not {
          be(None)
        }
        event.tags.get(EventKeys.TransactionName).get must be("Message Not Created")
      }

      withClue("event detail.messageId") {
        event.detail.get("messageId") must not {
          be(None)
        }
        event.detail.get("messageId").get must be("")
      }

      withClue("event detal.fhddsRef") {
        event.detail.get("fhddsRef") must not {
          be(None)
        }
        event.detail.get("fhddsRef").get must include regex ("XZFH00000100024")
      }

      withClue("event details.externalRef") {
        event.detail.get("externalRef") must not {
          be(None)
        }
        event.detail.get("externalRef").get must include regex ("^[0-9a-fA-F-]+$")
      }

      withClue("event detail.messageType") {
        event.detail.get("messageType") must not {
          be(None)
        }
        event.detail.get("messageType").get must be("fhddsAlertMessage")
      }

      withClue("event detail.reason") {
        event.detail.get("reason") must not {
          be(None)
        }
        event.detail.get("reason").get must be("this is the reason")
      }
    }
  }

  trait TestCaseV2 {
    implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
    val auditConnectorMock: AuditConnector = mock[AuditConnector]
    val secureMessageServiceMock = mock[SecureMessageService]
    lazy val customerAdviceAudit = new CustomerAdviceAudit(auditConnectorMock)

    val controller = new SecureMessageController(
      controllerComponents,
      customerAdviceAudit,
      secureMessageServiceMock,
      inboxPage,
      inboxPageV2,
      successPage,
      successPageV2,
      duplicatePage,
      duplicatePageV2,
      notPaperlessPage,
      unknownPage,
      unexpectedPage,
      unexpectedV2Page
    )(appConfig, ec)

    when(auditConnectorMock.sendEvent(any())(any(), any())).thenReturn(Future.successful(AuditResult.Success))

    val request = FakeRequest("POST", "/customer-advisors-frontend/submit").withFormUrlEncodedBody(
      "content"                     -> "content",
      "subject"                     -> "subject",
      "recipientTaxidentifierName"  -> "HMRC-OBTDS-ORG",
      "recipientTaxidentifierValue" -> "XZFH00000100024",
      "recipientEmail"              -> "foo@bar.com",
      "recipientNameLine1"          -> "Mr. John Smith",
      "messageType"                 -> "fhddsAlertMessage",
      "alertQueue"                  -> "PRIORITY"
    )
  }
}
