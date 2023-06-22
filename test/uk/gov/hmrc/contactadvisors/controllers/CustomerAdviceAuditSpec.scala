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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{ reset, verify, when }
import org.scalatest.BeforeAndAfterEach
import org.scalatest.concurrent.ScalaFutures
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status.SEE_OTHER
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.Results.SeeOther
import play.api.mvc.{ MessagesControllerComponents, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers.{ defaultAwaitTimeout, status }
import uk.gov.hmrc.contactadvisors.FrontendAppConfig
import uk.gov.hmrc.contactadvisors.domain._
import uk.gov.hmrc.contactadvisors.service.SecureMessageService
import uk.gov.hmrc.contactadvisors.views.html.secureMessage._
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.EventKeys
import uk.gov.hmrc.play.audit.http.connector.{ AuditConnector, AuditResult }
import uk.gov.hmrc.play.audit.model.DataEvent

import scala.concurrent.{ ExecutionContext, Future }

class CustomerAdviceAuditSpec extends PlaySpec with ScalaFutures with GuiceOneAppPerSuite with BeforeAndAfterEach {

  implicit lazy override val app: Application = new GuiceApplicationBuilder()
    .configure(
      "auditing.enabled" -> true
    )
    .build()

  implicit val ec = scala.concurrent.ExecutionContext.Implicits.global
  val appConfig = app.injector.instanceOf[FrontendAppConfig]
  val controllerComponents = app.injector.instanceOf[MessagesControllerComponents]
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
  val secureMessageServiceMock = mock[SecureMessageService]
  val auditConnectorMock = mock[AuditConnector]
  val customerAdviceAudit = new CustomerAdviceAudit(auditConnectorMock)

  val request = FakeRequest("POST", "/inbox/123456789").withFormUrlEncodedBody(
    "subject" -> "New message subject",
    "message" -> "New message body"
  )

  "SecureMessageController" should {

    "audit the successful event" in new TestCase {
      when(secureMessageServiceMock.createMessage(any(), any())(any(), any()))
        .thenReturn(Future.successful(AdviceStored("1234")))
      when(auditConnectorMock.sendEvent(any[DataEvent])(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(AuditResult.Success))

      val dataEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      val result = controller.submit("123456789")(request).futureValue
      status(Future.successful(result)) must be(SEE_OTHER)

      verify(auditConnectorMock).sendEvent(dataEventCaptor.capture())(any(), any())

      val event = dataEventCaptor.getValue
      event.auditSource must be("customer-advisors-frontend")
      event.auditType must be("TxSucceeded")
      event.detail.get("messageId").get must be("1234")
      event.tags.get(EventKeys.TransactionName).get must be("Message Stored")
    }

    "audit the duplicate message event" in new TestCase {
      when(secureMessageServiceMock.createMessage(any(), any())(any(), any()))
        .thenReturn(Future.successful(AdviceAlreadyExists))
      val dataEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      val result = controller.submit("123456789")(request).futureValue
      result must be(SeeOther("/secure-message/inbox/123456789/duplicate"))
      verify(auditConnectorMock).sendEvent(dataEventCaptor.capture())(any(), any())

      val event = dataEventCaptor.getValue
      event.auditSource must be("customer-advisors-frontend")
      event.auditType must be("TxFailed")
      event.detail.get("reason").get must be("Duplicate Message Found")
      event.tags.get(EventKeys.TransactionName).get must be("Message Not Stored")
    }

    "audit the unknown tax id event" in new TestCase {
      when(secureMessageServiceMock.createMessage(any(), any())(any(), any()))
        .thenReturn(Future.successful(UnknownTaxId))
      val dataEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      val result: Result = controller.submit("123456789")(request).futureValue
      result must be(SeeOther("/secure-message/inbox/123456789/unknown"))

      verify(auditConnectorMock).sendEvent(dataEventCaptor.capture())(any(), any())

      val event = dataEventCaptor.getValue
      event.auditSource must be("customer-advisors-frontend")
      event.auditType must be("TxFailed")
      event.detail.get("reason").get must be("Unknown Tax Id")
      event.tags.get(EventKeys.TransactionName).get must be("Message Not Stored")
    }

    "audit the user not paperless event" in new TestCase {
      when(secureMessageServiceMock.createMessage(any(), any())(any(), any()))
        .thenReturn(Future.successful(UserIsNotPaperless))

      val dataEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      val result = controller.submit("123456789")(request).futureValue
      result must be(SeeOther("/secure-message/inbox/123456789/not-paperless"))

      verify(auditConnectorMock).sendEvent(dataEventCaptor.capture())(any(), any())

      val event = dataEventCaptor.getValue
      event.auditSource must be("customer-advisors-frontend")
      event.auditType must be("TxFailed")
      event.detail.get("reason").get must be("User is not paperless")
      event.tags.get(EventKeys.TransactionName).get must be("Message Not Stored")
    }

    "audit the unexpected error event" in new TestCase {
      when(secureMessageServiceMock.createMessage(any(), any())(any(), any()))
        .thenReturn(Future.successful(UnexpectedError("this is the reason")))
      when(auditConnectorMock.sendEvent(any[DataEvent])(any[HeaderCarrier], any[ExecutionContext])).thenReturn(Future.successful(AuditResult.Success))

      val dataEventCaptor: ArgumentCaptor[DataEvent] = ArgumentCaptor.forClass(classOf[DataEvent])
      controller.submit("123456789")(request).futureValue

      verify(auditConnectorMock).sendEvent(dataEventCaptor.capture())(any(), any())

      val event = dataEventCaptor.getValue
      event.auditSource must be("customer-advisors-frontend")
      event.auditType must be("TxFailed")
      event.detail.get("reason").get must be("Unexpected Error: this is the reason")
      event.tags.get(EventKeys.TransactionName).get must be("Message Not Stored")
    }
  }

  trait TestCase {
    reset(auditConnectorMock, secureMessageServiceMock)

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
  }
}
