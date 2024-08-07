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
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneAppPerSuite
import play.api.Application
import play.api.http.Status
import play.api.i18n.MessagesApi
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{ MessagesControllerComponents, Result }
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.contactadvisors.FrontendAppConfig
import uk.gov.hmrc.contactadvisors.dependencies.{ EntityResolverStub, MessageStub }
import uk.gov.hmrc.contactadvisors.service.SecureMessageService
import uk.gov.hmrc.contactadvisors.views.html.secureMessage._
import uk.gov.hmrc.domain.SaUtr
import uk.gov.hmrc.utils.{ SecureMessageCreator, WithWiremock }

import java.util.UUID
import scala.concurrent.{ ExecutionContext, Future }
import scala.jdk.CollectionConverters._

class SecureMessageControllerSpec
    extends PlaySpec with GuiceOneAppPerSuite with ScalaFutures with IntegrationPatience with WithWiremock
    with EntityResolverStub with MessageStub {

  implicit lazy override val app: Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.message.port"         -> "10100",
      "microservice.services.entity-resolver.port" -> "10100"
    )
    .build()
  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.Implicits.global

  val getRequest = FakeRequest("GET", "/")
  val postRequest = FakeRequest("POST", "/")
  val customer_utr = UUID.randomUUID.toString

  val utr = SaUtr("123456789")
  val subject = "This is a response to your HMRC request"
  val adviceBody = "<p>This is the content of the secure message</p>"

  val controllerComponents = app.injector.instanceOf[MessagesControllerComponents]

  val customerAdviceAudit = app.injector.instanceOf[CustomerAdviceAudit]
  val secureMessageService = app.injector.instanceOf[SecureMessageService]
  val appConfig = app.injector.instanceOf[FrontendAppConfig]
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
  )(app.injector.instanceOf[FrontendAppConfig], ec) {
    def auditSource: String = "customer-advisors-frontend"
  }
  val externalRefID = secureMessageService.generateExternalRefID

  "GET /inbox/:utr" should {
    "return 200" in {
      val result = controller.inbox(customer_utr)(getRequest)
      status(result) must be(Status.OK)
    }

    "return HTML" in {
      val result = controller.inbox(customer_utr)(getRequest)
      contentType(result) must be(Some("text/html"))
      charset(result) must be(Some("utf-8"))
    }

    "show main banner" in {
      val result = controller.inbox(customer_utr)(getRequest)
      val document = Jsoup.parse(contentAsString(result))
      document.getElementsByTag("header").html().contains("govuk-header__logotype") must be(true)
    }

    "have the expected elements on the form" in {
      val result = controller.inbox(customer_utr)(getRequest)
      status(result) must be(200)

      val document = Jsoup.parse(contentAsString(result))
      val form = document.select("form#form-submit-customer-advice").get(0)
      val formElements = form.getAllElements.asScala

      val adviceSubject = formElements.find(_.id() == "subject")
      withClue("advice subject field") {
        Inside.inside(adviceSubject) { case Some(element) =>
          element.tagName() must be("input")
          element.attr("type") must be("hidden")
          element.`val`() must be("Response to your enquiry from HMRC customer services")
        }
      }

      val adviceMessage = formElements.find(_.id() == "message")
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
      status(result) must be(200)

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

  "POST /inbox/:utr" should {
    "indicate a bad request when any of the form elements are empty" in {
      val emptySubject = controller.submit(customer_utr)(
        FakeRequest().withFormUrlEncodedBody(
          "message" -> "A message success to the customer. lkasdfjas;ldfjk"
        )
      )

      Jsoup
        .parse(contentAsString(emptySubject))
        .getElementsByClass("govuk-input govuk-input--error")
        .asScala must have size 1
      status(emptySubject) must be(BAD_REQUEST)

      val emptyMessage = controller.submit(customer_utr)(
        FakeRequest().withFormUrlEncodedBody(
          "subject" -> "subject"
        )
      )

      Jsoup
        .parse(contentAsString(emptyMessage))
        .getElementsByClass("govuk-textarea govuk-textarea--error")
        .asScala must have size 1
      status(emptyMessage) must be(BAD_REQUEST)

      val emptyFormFields = controller.submit(customer_utr)(FakeRequest())

      Jsoup.parse(contentAsString(emptyFormFields)).getElementsByClass("govuk-form-group").asScala must have size 2
      status(emptyFormFields) must be(BAD_REQUEST)
    }

    "Leave script tags in the message and subject" in {
      givenEntityResolverReturnsAPaperlessUser(utr.value)
      givenMessageRespondsWith(SecureMessageCreator.uncleanMessage, successfulResponse)

      val xssMessage = controller.submit(utr.value)(
        FakeRequest(routes.SecureMessageController.submit(utr.value)).withFormUrlEncodedBody(
          "subject" -> "This is a response to your HMRC request<script>alert('hax')</script>",
          "message" -> "<p>This is the content of the secure message</p><script>alert('more hax')</script>"
        )
      )

      xssMessage.returnsRedirectTo(s"/inbox/$utr/success")
    }

    "redirect to the success page when the form submission is successful" in {
      givenEntityResolverReturnsAPaperlessUser(utr.value)

      givenMessageRespondsWith(SecureMessageCreator.message, successfulResponse)

      submissionOfCompletedForm().returnsRedirectTo(s"/inbox/$utr/success")
    }

    "redirect and indicate a duplicate message submission" in {
      givenEntityResolverReturnsAPaperlessUser(utr.value)
      givenMessageRespondsWith(SecureMessageCreator.message, duplicatedMessage)

      submissionOfCompletedForm().returnsRedirectTo(s"/inbox/$utr/duplicate")
    }

    "redirect and indicate an unexpected error has occurred when processing the submission" in {
      givenEntityResolverReturnsAPaperlessUser(utr.value)
      givenMessageRespondsWith(SecureMessageCreator.message, unknownTaxId)

      submissionOfCompletedForm().returnsRedirectTo(s"/inbox/$utr/unexpected")
    }

    "redirect and indicate that the user has not opted in for paperless communications" in {
      givenEntityResolverReturnsANonPaperlessUser(utr.value)

      submissionOfCompletedForm().returnsRedirectTo(s"/inbox/$utr/not-paperless")
    }

    "redirect and indicate a submission for unknown utr" in {
      givenEntityResolverReturnsNotFound(utr.value)

      submissionOfCompletedForm().returnsRedirectTo(s"/inbox/$utr/unknown")
    }

  }

  "submission result page" should {
    "contain correct message for success" in {
      controller
        .success(utr.value)(getRequest)
        .shouldContainPageWithTitleAndSuccessMessage(
          "Advice creation successful",
          utr.value,
          "Thanks. Your reply has been successfully received by the customer's Tax Account secure message Inbox."
        )
    }
    "contain correct message for duplicate" in {
      controller
        .duplicate(utr.value)(getRequest)
        .shouldContainPageWithTitleAndMessage(
          "Advice already exists",
          utr.value,
          "This message appears to be a duplicate of a message already received in the customer's Tax Account secure message Inbox."
        )
    }
    "contain correct message for unknown taxid" in {
      controller
        .unknown(utr.value)(getRequest)
        .shouldContainPageWithTitleAndMessage(
          "Unknown UTR",
          utr.value,
          "The SA-UTR provided is not recognised by the Digital Tax Platform."
        )
    }
    "contain correct message for not paperless user" in {
      controller
        .notPaperless(utr.value)(getRequest)
        .shouldContainPageWithTitleAndMessage(
          "User is not paperless",
          utr.value,
          s"The customer is not registered for paperless communications."
        )
    }
    "contain correct message for unexpected error" in {
      controller
        .unexpected(utr.value)(getRequest)
        .shouldContainPageWithTitleAndMessage(
          "Unexpected error",
          utr.value,
          "There is an unexpected problem. There may be an issue with the connection. Please try again."
        )
    }
  }

  def submissionOfCompletedForm() = controller.submit(utr.value)(
    FakeRequest(routes.SecureMessageController.submit(utr.value)).withFormUrlEncodedBody(
      "subject" -> subject,
      "message" -> adviceBody
    )
  )

  implicit class ShouldContainPageWithMessage(result: Future[Result]) {
    def shouldContainPageWithTitleAndMessage(title: String, utr: String, message: String) = {
      status(result) must be(200)
      contentType(result) must be(Some("text/html"))
      charset(result) must be(Some("utf-8"))

      val document = Jsoup.parse(contentAsString(result))
      document.getElementsByTag("header").html().contains("govuk-header__logotype") must be(true)

      withClue("result page title") {
        document.title() must be(title)
      }

      withClue("result page header") {
        document.select("h2").text().trim must include(s"Reply to customer with SA-UTR $utr")
      }

      withClue("result message") {
        val creationResult = document.select("p.govuk-error-message")
        creationResult must have size 1
        creationResult.get(0).text() must be(message)
      }
    }

    def shouldContainPageWithTitleAndSuccessMessage(title: String, utr: String, message: String) = {
      status(result) must be(200)
      contentType(result) must be(Some("text/html"))
      charset(result) must be(Some("utf-8"))

      val document = Jsoup.parse(contentAsString(result))
      document.getElementsByTag("header").html().contains("govuk-header__logotype") must be(true)

      withClue("result page title") {
        document.title() must be(title)
      }

      withClue("result page header") {
        document.select("h2").text().trim must include(s"Reply to customer with SA-UTR $utr")
      }

      withClue("result message") {
        val creationResult = document.select("h2.govuk-notification-banner__title")
        creationResult must have size 1
        creationResult.get(0).text() must be(message)
      }
    }

  }

  implicit class ReturnsRedirectTo(result: Future[Result]) {
    def returnsRedirectTo(url: String) = {
      status(result) must be(303)

      redirectLocation(result) match {
        case Some(redirect) => redirect must startWith(s"/secure-message$url")
        case _              => fail("redirect location should always be present")
      }

    }
  }
  override val dependenciesPort: Int = 10100
}
