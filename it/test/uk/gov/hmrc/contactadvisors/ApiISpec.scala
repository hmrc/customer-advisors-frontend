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

import java.time.LocalDate
import org.jsoup.Jsoup
import org.scalatest.BeforeAndAfterAll
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.{ Milliseconds, Seconds, Span }
import org.scalatestplus.play.PlaySpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.http.Status._
import play.api.libs.ws.WSClient
import uk.gov.hmrc.contactadvisors.UrlHelper.-/
import uk.gov.hmrc.http.HeaderCarrier
import play.api.libs.ws.DefaultBodyWritables.writeableOf_urlEncodedSimpleForm
import scala.concurrent.duration._

class ApiISpec extends PlaySpec with ScalaFutures with BeforeAndAfterAll with Eventually with GuiceOneServerPerSuite {

  def externalServices: Seq[String] = Seq.empty

  protected def startTimeout: FiniteDuration = 240.seconds

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "POST /customer-advisors-frontend/submit" should {
    "redirect to the success page when the form submission is successful" in {
      val content = LocalDate.now().toString
      val fhddsRef = "XZFH00000100024"
      val wsClient = app.injector.instanceOf[WSClient]
      val response = wsClient
        .url(resource("secure-message/customer-advisors-frontend/submit"))
        .post(
          Map(
            "content"                     -> s"${content}21",
            "subject"                     -> "mysubject",
            "recipientTaxidentifierName"  -> "sautr",
            "recipientTaxidentifierValue" -> fhddsRef,
            "recipientEmail"              -> "test@test.com",
            "recipientNameLine1"          -> "line1",
            "messageType"                 -> "mType"
          )
        )
        .futureValue(timeout = timeout(Span(2, Seconds)))

      response.status must be(OK)

      val body = response.body

      val document = Jsoup.parse(body)

      withClue("result page title") {
        document.title() must be("Advice creation successful")
      }
      withClue("result page FHDDS Reference") {
        document.select(".govuk-list li").get(0).text() must include(s"FHDDS Reference:")
      }
      withClue("result page Message Id") {
        document.select(".govuk-list li").get(1).text() must include("Id:")
      }
      withClue("result page External Ref") {
        document.select(".govuk-list li").get(2).text() must include("External Ref:")
      }
    }

    "redirect to the unexpected page when the form submission is unsuccessful" in {

      val content = LocalDate.now().toString
      val fhddsRef = "XZFH00000100024"
      val wrongEmail = "foobar"
      val wsClient = app.injector.instanceOf[WSClient]

      val response = wsClient
        .url(resource("secure-message/customer-advisors-frontend/submit"))
        .post(
          Map(
            "content"                     -> s"${content}21",
            "subject"                     -> "mysubject",
            "recipientTaxidentifierName"  -> "sautr",
            "recipientTaxidentifierValue" -> s"$fhddsRef",
            "recipientEmail"              -> s"$wrongEmail",
            "recipientNameLine1"          -> "rLine1",
            "messageType"                 -> "mType"
          )
        )
        .futureValue(timeout = timeout(Span(2, Seconds)))

      response.status must be(OK)
      val body = response.body
      val document = Jsoup.parse(body)

      withClue("result page title") {
        document.title() must be("Unexpected error")
      }

      withClue("result page title") {
        document.title() must be("Unexpected error")
      }
      withClue("result page h2") {
        document.select("h2").text().trim must include(s"Failed")
      }
      withClue("result page alert message") {
        document.select("p.govuk-error-message").text() must include(s"There is an unexpected problem")
      }
    }
  }

  def resource(path: String): String =
    s"http://localhost:$port/${-/(path)}"
}

object UrlHelper {
  def -/(uri: String) =
    if (uri.startsWith("/")) uri.drop(1) else uri
}
