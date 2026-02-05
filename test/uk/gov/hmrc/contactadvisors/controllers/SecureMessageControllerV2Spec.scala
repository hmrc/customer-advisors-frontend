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

import org.jsoup.Jsoup
import org.scalatest.Inside
import org.scalatest.concurrent.{ IntegrationPatience, ScalaFutures }
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.mvc.{ AnyContentAsEmpty, MessagesControllerComponents, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import uk.gov.hmrc.contactadvisors.FrontendAppConfig
import uk.gov.hmrc.contactadvisors.dependencies.MessageStubV2
import uk.gov.hmrc.contactadvisors.service.SecureMessageService
import uk.gov.hmrc.contactadvisors.views.html.secureMessage.*
import uk.gov.hmrc.utils.{ SecureMessageCreatorV2, SpecBase, WithWiremock }

import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters.*

class SecureMessageControllerV2Spec
    extends SpecBase with GuiceOneAppPerSuite with ScalaFutures with IntegrationPatience with WithWiremock
    with MessageStubV2 {

  implicit lazy override val app: Application = applicationBuilder
    .configure(
      "microservice.services.message.port"         -> "10100",
      "microservice.services.entity-resolver.port" -> "10100"
    )
    .build()

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val getRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/")
  val postRequest: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("POST", "/")

  val subject = "This is a response to your HMRC request"
  val content = "A message success to the customer."
  val recipientTaxidentifierName = "HMRC-OBTDS-ORG"
  val recipientTaxidentifierValue = "XZFH00000100024"
  val recipientEmail = "foo@bar.com"
  val recipientNameLine1 = "Mr. John Smith"
  val messageType = "fhddsAlertMessage"
  val adviceBody = "<p>This is the content of the secure message</p>"

  val messagesApi: MessagesApi = instanceOf[MessagesApi](app)
  val appConfig: FrontendAppConfig = instanceOf[FrontendAppConfig](app)
  val controllerComponents: MessagesControllerComponents = instanceOf[MessagesControllerComponents](app)
  val messageApi: MessagesApi = instanceOf[MessagesApi](app)

  val customerAdviceAudit: CustomerAdviceAudit = instanceOf[CustomerAdviceAudit](app)
  val secureMessageService: SecureMessageService = instanceOf[SecureMessageService](app)

  val inboxPage: Inbox = instanceOf[Inbox](app)
  val inboxPageV2: InboxV2 = instanceOf[InboxV2](app)
  val successPage: Success = instanceOf[Success](app)
  val successPageV2: SuccessV2 = instanceOf[SuccessV2](app)
  val duplicatePage: Duplicate = instanceOf[Duplicate](app)
  val duplicatePageV2: DuplicateV2 = instanceOf[DuplicateV2](app)
  val notPaperlessPage: Not_paperless = instanceOf[Not_paperless](app)
  val unknownPage: Unknown = instanceOf[Unknown](app)
  val unexpectedPage: Unexpected = instanceOf[Unexpected](app)
  val unexpectedV2Page: UnexpectedV2 = instanceOf[UnexpectedV2](app)

  val externalRefID: String = secureMessageService.generateExternalRefID

  val controller = new SecureMessageController(
    controllerComponents,
    customerAdviceAudit,
    secureMessageService,
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

  "GET /customer-advisors-frontend/inbox" should {
    "return 200" in {
      val result = controller.inboxV2()(getRequest)
      status(result) must be(Status.OK)
    }

    "return HTML" in {
      val result = controller.inboxV2()(getRequest)
      contentType(result) must be(Some("text/html"))
      charset(result) must be(Some("utf-8"))
    }

    "show main banner" in {
      val result = controller.inboxV2()(getRequest)
      val document = Jsoup.parse(contentAsString(result))
      document.getElementsByTag("header").html().contains("govuk-header__logotype") must be(true)
    }

    "have the expected elements on the form" in {
      val result = controller.inboxV2()(getRequest)
      status(result) must be(OK)

      val document = Jsoup.parse(contentAsString(result))
      val form = document.select("form#form-submit-customer-advice").get(0)
      val formElements = form.getAllElements.asScala

      val adviceSubject = formElements.find(_.id() == "subject")
      withClue("advice subject field") {
        Inside.inside(adviceSubject) { case Some(element) =>
          element.tagName() must be("input")
        }
      }

      val adviceMessage = formElements.find(_.id() == "content")
      withClue("advice message field") {
        adviceMessage.map(_.tagName()) must be(Some("textarea"))
      }

      val submitAdvice = formElements.find(_.id() == "submit-advice")
      withClue("submit advice button") {
        submitAdvice.map(_.tagName()) must be(Some("button"))
        submitAdvice.map(_.text()) must be(Some("Send"))
      }
    }
  }

  "POST /customer-advisors-frontend/submit" should {
    "indicate a bad request when any of the form elements are empty" in {
      val emptySubject = controller.submitV2()(
        FakeRequest(routes.SecureMessageController.submitV2()).withFormUrlEncodedBody(
          "content"                     -> content,
          "recipientTaxidentifierName"  -> recipientTaxidentifierName,
          "recipientTaxidentifierValue" -> recipientTaxidentifierValue,
          "recipientEmail"              -> recipientEmail,
          "recipientNameLine1"          -> recipientNameLine1,
          "messageType"                 -> messageType
        )
      )

      Jsoup.parse(contentAsString(emptySubject)).getElementsByClass("govuk-error-message").asScala must have size 1
      status(emptySubject) must be(BAD_REQUEST)

      val emptyMessage = controller.submitV2()(
        FakeRequest(routes.SecureMessageController.submitV2()).withFormUrlEncodedBody(
          "subject"                     -> subject,
          "recipientTaxidentifierName"  -> recipientTaxidentifierName,
          "recipientTaxidentifierValue" -> recipientTaxidentifierValue,
          "recipientEmail"              -> recipientEmail,
          "recipientNameLine1"          -> recipientNameLine1,
          "messageType"                 -> messageType
        )
      )

      Jsoup.parse(contentAsString(emptyMessage)).getElementsByClass("govuk-error-message").asScala must have size 1
      status(emptyMessage) must be(BAD_REQUEST)

      val emptyFormFields = controller.submitV2()(FakeRequest())

      Jsoup.parse(contentAsString(emptyFormFields)).getElementsByClass("govuk-form-group").asScala must have size 7
      status(emptyFormFields) must be(BAD_REQUEST)
    }

    "redirect to the success page when the form submission is successful" in {
      val advice = SecureMessageCreatorV2.adviceWithUncleanContent

      givenMessageRespondsWith(advice, successfulResponse)

      val xssMessage = controller.submitV2()(
        FakeRequest(routes.SecureMessageController.submitV2()).withFormUrlEncodedBody(
          "content"                     -> advice.content,
          "subject"                     -> advice.subject,
          "recipientTaxidentifierName"  -> advice.recipientTaxidentifierName,
          "recipientTaxidentifierValue" -> advice.recipientTaxidentifierValue,
          "recipientEmail"              -> advice.recipientEmail,
          "recipientNameLine1"          -> advice.recipientNameLine1,
          "messageType"                 -> advice.messageType
        )
      )

      xssMessage.returnsRedirectTo(s"/customer-advisors-frontend/inbox/success")
    }
    "Leave script tags in the message and subject" in {
      val advice = SecureMessageCreatorV2.adviceWithUncleanContent

      givenMessageRespondsWith(advice, successfulResponse)

      val xssMessage = controller.submitV2()(
        FakeRequest(routes.SecureMessageController.submitV2()).withFormUrlEncodedBody(
          "content"                     -> advice.content,
          "subject"                     -> advice.subject,
          "recipientTaxidentifierName"  -> advice.recipientTaxidentifierName,
          "recipientTaxidentifierValue" -> advice.recipientTaxidentifierValue,
          "recipientEmail"              -> advice.recipientEmail,
          "recipientNameLine1"          -> advice.recipientNameLine1,
          "messageType"                 -> advice.messageType
        )
      )

      xssMessage.returnsRedirectTo(s"/customer-advisors-frontend/inbox/success")
    }

    "redirect and indicate a duplicate message submission" in {
      val advice = SecureMessageCreatorV2.adviceWithCleanContent
      givenMessageRespondsWith(advice, duplicatedMessage)

      val xssMessage = controller.submitV2()(
        FakeRequest(routes.SecureMessageController.submitV2()).withFormUrlEncodedBody(
          "content"                     -> advice.content,
          "subject"                     -> advice.subject,
          "recipientTaxidentifierName"  -> advice.recipientTaxidentifierName,
          "recipientTaxidentifierValue" -> advice.recipientTaxidentifierValue,
          "recipientEmail"              -> advice.recipientEmail,
          "recipientNameLine1"          -> advice.recipientNameLine1,
          "messageType"                 -> advice.messageType
        )
      )

      xssMessage.returnsRedirectTo("/customer-advisors-frontend/inbox/duplicate")
    }

    "redirect and indicate an unexpected error has occurred when processing the submission" in {
      val advice = SecureMessageCreatorV2.adviceWithCleanContent
      givenMessageRespondsWith(advice, (1, "asdfasdf"))

      val xssMessage = controller.submitV2()(
        FakeRequest(routes.SecureMessageController.submitV2()).withFormUrlEncodedBody(
          "content"                     -> advice.content,
          "subject"                     -> advice.subject,
          "recipientTaxidentifierName"  -> advice.recipientTaxidentifierName,
          "recipientTaxidentifierValue" -> advice.recipientTaxidentifierValue,
          "recipientEmail"              -> advice.recipientEmail,
          "recipientNameLine1"          -> advice.recipientNameLine1,
          "messageType"                 -> advice.messageType
        )
      )

      xssMessage.returnsRedirectTo("/customer-advisors-frontend/inbox/unexpected")
    }

  }
  "submission result page" should {
    "contain correct message for success" in {
      val result = controller.successV2()(getRequest)

      status(result) must be(OK)
      contentType(result) must be(Some("text/html"))
      charset(result) must be(Some("utf-8"))
      val document = Jsoup.parse(contentAsString(result))

      document.getElementsByTag("header").html().contains("govuk-header__logotype") must be(true)

      withClue("result page title") {
        document.title() must be("Advice creation successful")
      }
      withClue("result page h2") {
        document.select("h2").text().trim must include(s"Success")
      }
    }
    "contain correct message for duplicate" in {
      val result = controller.duplicateV2()(getRequest)
      status(result) must be(OK)
      contentType(result) must be(Some("text/html"))
      charset(result) must be(Some("utf-8"))
      val document = Jsoup.parse(contentAsString(result))
      document.getElementsByTag("header").html().contains("govuk-header__logotype") must be(true)

      withClue("result page title") {
        document.title() must be("Advice already exists")
      }
    }

    "contain correct message for unexpected error" in {
      val result = controller.unexpectedV2()(getRequest)
      status(result) must be(OK)
      contentType(result) must be(Some("text/html"))
      charset(result) must be(Some("utf-8"))
      val document = Jsoup.parse(contentAsString(result))
      document.getElementsByTag("header").html().contains("govuk-header__logotype") must be(true)

      withClue("result page title") {
        document.title() must be("Unexpected error")
      }
      withClue("result page h2") {
        document.select("h2").text().trim must include(s"Failed")
      }
    }
  }
  "Unexpected page" must {
    "include taxId" in {
      val request = FakeRequest("GET", "/customer-advisors-frontend/inbox/unexpected").withFlash(("taxid", "123456789"))
      val result = controller.unexpectedV2()(request)
      contentAsString(result) must include("123456789")
    }
  }

  "SuccessV2 page" must {
    "include taxId, messageId and externalRef" in {
      val request =
        FakeRequest("GET", "/customer-advisors-frontend/inbox/success")
          .withFlash(("taxid", "123456789"), ("messageId", "8888"), ("externalRef", "9999"))
      val result = controller.successV2()(request)
      val pageContent = contentAsString(result)
      pageContent must include("123456789")
      pageContent must include("8888")
      pageContent must include("9999")
    }
  }

  "DuplicateV2 page" must {
    "include taxId" in {
      val request = FakeRequest("GET", "/customer-advisors-frontend/inbox/duplicate").withFlash(("taxid", "123456789"))
      val result = controller.duplicateV2()(request)
      val pageContent = contentAsString(result)
      pageContent must include("123456789")
    }
  }

  def submissionOfCompletedForm(): Future[Result] = controller.submitV2()(
    FakeRequest().withFormUrlEncodedBody(
      "content"                     -> "content",
      "subject"                     -> "subject",
      "recipientTaxidentifierName"  -> "name",
      "recipientTaxidentifierValue" -> "value",
      "recipientEmail"              -> "foo@bar.com",
      "recipientNameLine1"          -> "Mr. John Smith",
      "messageType"                 -> "fhddsAlertMessage",
      "alertQueue"                  -> "PRIORITY"
    )
  )

  implicit class ReturnsRedirectTo(result: Future[Result]) {
    def returnsRedirectTo(url: String) = {
      status(result) must be(SEE_OTHER)
      redirectLocation(result) match {
        case Some(redirect) => redirect must (startWith(s"/secure-message$url"))
        case _              => fail("redirect location should always be present")
      }
    }
  }
  override val dependenciesPort: Int = 10100
}
